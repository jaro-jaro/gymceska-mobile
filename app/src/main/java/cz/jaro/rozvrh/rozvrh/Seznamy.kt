package cz.jaro.rozvrh.rozvrh

object Seznamy {
    val tridy = listOf(
        "Třídy",
        "1.A",
        "1.E",
        "1.S",
        "2.A",
        "2.E",
        "2.S",
        "3.A",
        "3.E",
        "3.S",
        "4.A",
        "4.E",
        "4.S",
        "5.E",
        "6.E",
        "7.E",
        "8.E",
    )
    val vyucujici = listOf(
        "Vyučující",
        "Adamcová Eva",
        "Bahenský Petr",
        "Bártů Jana",
        "Beníšek Petr",
        "Boček Lukáš",
        "Černá Marie",
        "Chytil Alena",
        "Čoudková Štěpánka",
        "Couf Jiří",
        "Dolejší Petr",
        "Dřevikovská Pavla",
        "Dvořák Jan",
        "Dvořák Jan",
        "Filip Jiří",
        "Graman Miroslav",
        "Habertová Petra",
        "Hornátová Lucie",
        "Houšková Pavlína",
        "Hrubešová Radka",
        "Hýsek Miroslav",
        "Jirsová Žaneta",
        "Kotlas Miroslav",
        "Kovaříková Eva",
        "Kratochvílová Tereza",
        "Krhounková Věra",
        "Křížová Radka",
        "Lafata David",
        "Ludvíková Klára",
        "Mikeš Vladimír",
        "Musilová Hana",
        "Mušková Marie",
        "Najbrt Tomáš",
        "Nečilová Lenka",
        "Nová Petra",
        "Paukert Roman",
        "Petřeková Jaroslava",
        "Pfefrčková Michala",
        "Píša Adam",
        "Pitrunová Ester",
        "Pokorná Ivana",
        "Radová Marcela",
        "Regulová Věra",
        "Řezníčková Zdeňka",
        "Rožboud Stanislav",
        "Šátavová Tereza",
        "Schönová Alena",
        "Sekyrka Antonín",
        "Sekyrka Vlastimil",
        "Sekyrková Miroslava",
        "Šimonek Milan",
        "Špišáková Petra",
        "Štěpánková Hana",
        "Štoudek Martin",
        "Štoudková Vladislava",
        "Suchý Jan",
        "Syslová Zuzana",
        "Týmalová Simona",
        "Uhlík Michal",
        "Vaníček Miroslav",
        "Weber Oldřich",
        "Zach Vojtěch",
        "Zahradníčková Zita",
        "Zelenka Petj",
    )

    val mistnosti = listOf(
        "Místnosti",
        "1.A",
        "1.E",
        "1.S",
        "2.A",
        "2.E",
        "2.S",
        "3.A",
        "3.E",
        "3.S",
        "4.A",
        "4.E",
        "4.S",
        "5.E",
        "6.E",
        "7.E",
        "8.E",
        "Aula",
        "C1",
        "C2",
        "C3",
        "Hv",
        "Inf1",
        "Inf2",
        "LBi",
        "LCh",
        "Tv",
        "Vv",
    )

    val stalost = listOf(
        "Tento týden",
        "Příští týden",
        "Stálý",
    )
    val stalost2 = listOf(
        "tento týden",
        "příští týden",
        "vždy",
    )
    val tridyOdkazy = listOf(
        "https://gymceska.bakalari.cz/timetable/public/###/class/UW?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UV?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UX?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UT?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UU?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/US?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UP?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UQ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UR?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UM?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UO?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UN?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UJ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UI?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UC?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/class/UA?TouchMode=1",
    )

    val vyucujiciOdkazy = listOf(
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_115?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__77?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UTZD0?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_116?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UQZCO?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZDE?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UOZC7?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZB1?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__79?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UNZC1?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UQZCT?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_133?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPZCF?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__46?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__52?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_130?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZDB?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZAJ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZAT?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_132?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UTZD1?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/ULZBS?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UJZB9?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPZCD?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U17RV?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_138?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UTZCZ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZAH?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__87?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UQZCR?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UTZD2?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__80?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__96?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZDA?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/USZCX?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__98?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UKZBI?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UVZD7?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UKTHD?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__54?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UXAPU?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPBVZ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UVZD6?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/USZCW?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZD9?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPZCG?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UMZBW?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UKZBL?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/URZCU?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U__76?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPZCM?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UQZCS?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZAR?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/ULZBV?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UUZD3?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UZZAY?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UOZC8?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZDD?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/U_123?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UOZC6?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UTZCY?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UPZCC?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/teacher/UWZDC?TouchMode=1",
    )

    val mistnostiOdkazy = listOf(
        "https://gymceska.bakalari.cz/timetable/public/###/room/06?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/LG?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/01?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/DT?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/G4?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/02?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/07?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/VF?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/04?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/32?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/C0?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/03?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/3Z?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/5H?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/U2?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/00?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/08?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/ZW?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/A5?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/05?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/GZ?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/ZX?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/84?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/35?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/OG?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/RR?TouchMode=1",
        "https://gymceska.bakalari.cz/timetable/public/###/room/6B?TouchMode=1",
    )

    val stalostOdkazy = listOf(
        "Actual",
        "Next",
        "Permanent",
    )

    val vyucujiciZkratky = listOf(
        "Ada", //Adamcová Eva
        "Bah", //Bahenský Petr
        "Bart", //Bártů Jana
        "Ben", //Beníšek Petr
        "Boc", //Boček Lukáš
        "Cer", //Černá Marie
        "Chyt", //Chytil Alena
        "Čou", //Čoudková Štěpánka
        "Cou", //Couf Jiří
        "Dol", //Dolejší Petr
        "Dře", //Dřevikovská Pavla
        "Dvk", //Dvořák Jan
        "Dvo", //Dvořák Jan
        "Fil", //Filip Jiří
        "Gra", //Graman Miroslav
        "Hab", //Habertová Petra
        "Hor", //Hornátová Lucie
        "Hou", //Houšková Pavlína
        "Hru", //Hrubešová Radka
        "Hys", //Hýsek Miroslav
        "Jir", //Jirsová Žaneta
        "Kot", //Kotlas Miroslav
        "Kov", //Kovaříková Eva
        "Kra", //Kratochvílová Tereza
        "Krh", //Krhounková Věra
        "Kří1", //Křížová Radka
        "Laf", //Lafata David
        "Lud", //Ludvíková Klára
        "Mik", //Mikeš Vladimír
        "Mus", //Musilová Hana
        "Muk", //Mušková Marie
        "Naj", //Najbrt Tomáš
        "Neč", //Nečilová Lenka
        "Nova", //Nová Petra
        "Pau", //Paukert Roman
        "Pet", //Petřeková Jaroslava
        "Pfe", //Pfefrčková Michala
        "Pia", //Píša Adam
        "Pit", //Pitrunová Ester
        "Pok", //Pokorná Ivana
        "Rad", //Radová Marcela
        "Reg", //Regulová Věra
        "Ren", //Řezníčková Zdeňka
        "Roz", //Rožboud Stanislav
        "Šat", //Šátavová Tereza
        "Scho", //Schönová Alena
        "Sea", //Sekyrka Antonín
        "Sek", //Sekyrka Vlastimil
        "Sey", //Sekyrková Miroslava
        "Šim", //Šimonek Milan
        "Špi", //Špišáková Petra
        "Ště", //Štěpánková Hana
        "Štd", //Štoudek Martin
        "Što", //Štoudková Vladislava
        "Such", //Suchý Jan
        "Sys", //Syslová Zuzana
        "Tym", //Týmalová Simona
        "Uhl", //
        "Van", //Vaníček Miroslav
        "Web", //Weber Oldřich
        "Zach", //Zach Vojtěch
        "Zah", //Zahradníčková Zita
        "Zel", //Zelenka Petr
    )

    val dny1Pad = listOf(
        "Pondělí",
        "Úterý",
        "Středa",
        "Čtvrtek",
        "Pátek",
    )
    val dny6Pad = listOf(
        "v pondělí",
        "v úterý",
        "ve středu",
        "ve čtvrtek",
        "v pátek",
    )
    val hodiny1Pad = listOf(
        "0. hodina",
        "1. hodina",
        "2. hodina",
        "3. hodina",
        "4. hodina",
        "5. hodina",
        "6. hodina",
        "7. hodina",
        "8. hodina",
        "9. hodina",
        "10. hodina",
        "11. hodina",
        "12. hodina",
        "13. hodina",
        "14. hodina",
        "15. hodina",
    )
    val hodiny4Pad = listOf(
        "0. hodinu",
        "1. hodinu",
        "2. hodinu",
        "3. hodinu",
        "4. hodinu",
        "5. hodinu",
        "6. hodinu",
        "7. hodinu",
        "8. hodinu",
        "9. hodinu",
        "10. hodinu",
        "11. hodinu",
        "12. hodinu",
        "13. hodinu",
        "14. hodinu",
        "15. hodinu",
    )
}
