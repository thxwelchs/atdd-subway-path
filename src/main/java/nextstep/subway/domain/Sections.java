package nextstep.subway.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.*;

@Embeddable
public class Sections {
    private final static int FIRST_SECTION_INDEX = 0;
    private final static int END_STATIONS_SIZE = 2;
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public Sections() {
    }

    public Section add(Section section) {
        if (isEmptySections()) {
            sections.add(section);
            return section;
        }

        if (isAlreadyRegisteredSection(section)) {
            throw new IllegalArgumentException("추가하려는 신규 구간의 상행역, 하행역이 기존 구간 중 이미 모두 존재합니다.");
        }

        if (isMiddleAdditionalMode(section)) {
            handleMiddleAdditionalMode(section);
        }

        sections.add(section);
        return section;
    }

    private void handleMiddleAdditionalMode(Section section) {
        Section existingSection = findByExistingSection(section)
                .orElseThrow(() -> new IllegalArgumentException("추가하려는 신규 구간의 상행역, 하행역 모두 기존 구간에 포함되어 있지 않습니다."));

        if (section.isGreaterThanOrEqualDistance(existingSection)) {
            throw new IllegalArgumentException("추가하려는 신규 구간의 역 사이 길이가 기존 구간의 역 사이 길이보다 크거나 같습니다.");
        }

        existingSection.updateToMiddleStation(section);
    }

    public void delete(Station station) {
        if (hasOnlyEndStations()) {
            throw new IllegalArgumentException("노선 구간에 종점역만 존재하여 더 이상 구간을 삭제할 수 없습니다.");
        }

        if (isNotEndDownStation(station)) {
            throw new IllegalArgumentException("삭제할 구간은 하행종점역 구간이 아닙니다.");
        }

        sections.remove(getLastSection().orElseThrow(() -> new IllegalArgumentException("더 이상 삭제할 구간이 존재하지 않습니다.")));
    }

    private Optional<Section> findByExistingSection(Section section) {
        return sections.stream()
                .filter(s -> s.isEqualsUpStation(section) || s.isEqualsDownStation(section))
                .findFirst();
    }

    private boolean isAlreadyRegisteredSection(Section newSection) {
        return sections.stream()
                .anyMatch(section -> section.isEqualsAllStations(newSection));
    }

    private boolean isNotEndDownStation(Station station) {
        return getEndDownStation().map(endDownStation -> !endDownStation.equals(station)).orElse(true);
    }

    private boolean isEmptySections() {
        return sections.isEmpty();
    }


    private Optional<Station> getEndDownStation() {
        if (!getLastSection().isPresent()) {
            return Optional.empty();
        }

        return Optional.of(getLastSection().get().getDownStation());
    }


    private boolean hasOnlyEndStations() {
        return flatStations().size() == END_STATIONS_SIZE;
    }

    private Optional<Section> getLastSection() {
        if (isEmptySections()) { //Defensive
            Optional.empty();
        }

        int sectionLastIndex = sections.size() - 1;

        return Optional.of(sections.get(sectionLastIndex));
    }

    private Section getFirstSection() {
        if (isEmptySections()) {
            throw new IllegalStateException("empty sections");
        }

        return sections.get(FIRST_SECTION_INDEX);
    }

    public Section findEndUpSection() {
        return CursorableSectionFinder.find(getFirstSection(), new EndUpSectionFindStrategy(sections));
    }

    public Section findEndDownSection() {
        return CursorableSectionFinder.find(getFirstSection(), new EndDownSectionFindStrategy(sections));
    }

    private boolean isMiddleAdditionalMode(Section section) {
        return !isEndSectionAdditionalMode(section);
    }

    private boolean isEndSectionAdditionalMode(Section section) {
        return findEndUpSection().isNext(section) || findEndDownSection().isPrevious(section);
    }

    public Station findEndUpStation() {
        return findEndUpSection().getUpStation();
    }

    public Station findEndDownStation() {
        return findEndDownSection().getDownStation();
    }

    public Set<Station> flatStations(Set<Station> stations, Section cursor) {
        stations.add(cursor.getUpStation());
        stations.add(cursor.getDownStation());

        Optional<Section> sectionOptional = sections.stream()
                .filter(cursor::isPrevious)
                .findFirst();

        return sectionOptional.isPresent() ? flatStations(stations, sectionOptional.get()) : stations;
    }

    public List<Station> flatStations() {
        return new ArrayList<>(flatStations(new LinkedHashSet<>(), findEndUpSection()));
    }
}