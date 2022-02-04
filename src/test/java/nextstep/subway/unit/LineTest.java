package nextstep.subway.unit;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.PairedStations;
import nextstep.subway.domain.Station;
import nextstep.subway.utils.EntityFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineTest {
    @DisplayName("동일한 역으로 구간 역을 설정하여 노선을 생성하면 예외가 발생한다")
    @Test
    void 동일한_역으로_구간생성시_예외_발생() {
        //when then
        assertThrows(IllegalArgumentException.class, () -> {
            상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 1L);
        });
    }

    @DisplayName("동일한 역으로 구간 역을 설정하여 노선의 구간을 추가하면 예외가 발생한다")
    @Test
    void 동일한_역으로_구간_추가시_예외_발생() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(2L);
        Station downStation = 역_생성(2L);

        //when then
        assertThrows(IllegalArgumentException.class, () -> {
            line.addSection(new PairedStations(upStation, downStation), 100);
        });
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, " +
            "새로 추가할 구간의 상행역이 기존 노선 하행종점역이고, " +
            "새로 추가할 구간의 하행역이 기존 노선에 등록되어 있지 않으면, " +
            "구간 추가가 예외없이 성공한다")
    @Test
    void 노선에_등록된_구간이_있고_구간을_추가할_때_정상() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(2L);
        Station downStation = 역_생성(3L);

        //when then
        assertDoesNotThrow(() -> line.addSection(new PairedStations(upStation, downStation), 1000));
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, " +
            "새로 추가할 구간의 상행역이 기존 노선 하행종점역이 아니면, " +
            "구간 추가시 예외가 발생한다")
    @Test
    void 노선에_등록된_구간이_있고_구간을_추가할_때_하행종점역_예외() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(1L);
        Station downStation = 역_생성(3L);

        //when then
        assertThrows(IllegalArgumentException.class, () -> line.addSection(new PairedStations(upStation, downStation), 1000));
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, " +
            "새로 추가할 구간의 하행역이 기존 노선에 등록되어있는 역이라면" +
            "구간 추가시 예외가 발생한다")
    @Test
    void 노선에_등록된_구간이_있고_구간을_추가할_때_이미_등록된역_예외() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(2L);
        Station downStation = 역_생성(1L);

        //when then
        assertThrows(IllegalArgumentException.class, () -> line.addSection(new PairedStations(upStation, downStation), 1000));
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, 구간 삭제시 예외가 발생한다")
    @Test
    void 노선에_등록된_구간_삭제_종점역만_등록되어있어_예외() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);

        //when then
        assertThrows(IllegalArgumentException.class, () -> line.deleteSection(역_생성(3L)));
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, 구간 삭제시 예외가 발생한다")
    @Test
    void 노선에_등록된_구간_삭제_삭제할_구간의_하행역이_종점역이_아니면_예외() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(2L);
        Station downStation = 역_생성(3L);
        line.addSection(new PairedStations(upStation, downStation), 100);

        //when then
        assertThrows(IllegalArgumentException.class, () -> line.deleteSection(역_생성(4L)));
    }

    @DisplayName("노선에 등록된 구간이 없고, 구간 삭제시 실패한다")
    @Test
    void 노선에_등록된_구간_삭제_삭제할_구간이_존재하지_않을때() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);

        //when then
        assertThrows(IllegalArgumentException.class, () -> line.deleteSection(역_생성(3L)));
    }

    @DisplayName("노선에 등록된 하행종점역, 상행종점역이 있고, 구간 삭제시 성공한다")
    @Test
    void 노선에_등록된_구간_삭제() {
        //given
        Line line = 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(1L, 2L);
        Station upStation = 역_생성(2L);
        Station downStation = 역_생성(3L);
        line.addSection(new PairedStations(upStation, downStation), 100);

        //when then
        assertThat(line.getStations().size()).isEqualTo(3);
        assertDoesNotThrow(() -> line.deleteSection(역_생성(3L)));
        assertThat(line.getStations().size()).isEqualTo(2);
        assertThat(line.getStations().stream().map(Station::getId)).containsExactly(1L, 2L);
    }

    private Line 상행종점역_하행종점역만_가진_구간이_포함된_노선_생성(Long upStationId, Long downStationId) {
        Station upStation = 역_생성(upStationId);
        Station downStation = 역_생성(downStationId);

        return new Line("2호선", "green", new PairedStations(upStation, downStation), 9999);
    }

    private Station 역_생성(Long id) {
        return EntityFixtures.createEntityFixtureWithId(id, Station.class);
    }
}
