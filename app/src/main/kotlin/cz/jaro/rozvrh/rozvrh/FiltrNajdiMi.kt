package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.rozvrh.FiltrNajdiMi.JenCele
import cz.jaro.rozvrh.rozvrh.FiltrNajdiMi.JenOdemcene
import cz.jaro.rozvrh.rozvrh.FiltrNajdiMi.JenSvi

enum class FiltrNajdiMi {
    JenOdemcene,
    JenCele,
    JenSvi,
}

fun List<FiltrNajdiMi>.text() = when {
    JenSvi in this -> "moji "
    JenCele in this && JenOdemcene in this -> "odemčené celé "
    JenOdemcene in this -> "odemčené "
    JenCele in this -> "celé "
    else -> ""
}