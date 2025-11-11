package br.com.dio.persistence.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.INITIAL;

@Data
public class BoardEntity {

    private Long id;
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BoardColumnEntity> boardColumns = new ArrayList<>();

    public BoardColumnEntity getInitialColumn() {
        return findColumnByKind(INITIAL);
    }

    public BoardColumnEntity getCancelColumn() {
        return findColumnByKind(CANCEL);
    }

    private BoardColumnEntity findColumnByKind(BoardColumnKindEnum kind) {
        return boardColumns.stream()
                .filter(column -> column.getKind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Column of kind " + kind + " not found"));
    }
}
