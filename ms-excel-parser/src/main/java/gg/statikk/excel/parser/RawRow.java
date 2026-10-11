package gg.statikk.excel.parser;

import java.util.List;

/** Fila tal como viene del archivo, todo como texto. number: la cabecera es la fila 1. */
public record RawRow(int number, List<String> cells) {}
