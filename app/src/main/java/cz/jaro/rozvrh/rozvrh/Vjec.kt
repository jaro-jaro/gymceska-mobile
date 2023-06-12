package cz.jaro.rozvrh.rozvrh

import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Aula
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.C1
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.C2
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.C3
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Hv
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Inf1
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Inf2
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.LBi
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.LCh
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Mistnosti
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Tv
import cz.jaro.rozvrh.rozvrh.Vjec.MistnostVjec.Vv
import cz.jaro.rozvrh.rozvrh.Vjec.TridaVjec.Tridy
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Ada
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Bah
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Bart
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Ben
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Boc
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Cer
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Chyt
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Cou
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Coud
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Dol
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Dre
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Dvk
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Dvo
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Fil
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Glm
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Gra
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Hab
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Hor
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Hou
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Hru
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Hys
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Jir
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Kov
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Kra
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Krh
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Kri1
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Laf
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Lud
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Mik
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Muk
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Mus
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Naj
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Nec
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Nova
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pau
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pet
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pfe
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pia
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pit
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Pok
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Rad
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Reg
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Ren
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Roz
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sat
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Scho
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sea
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sek
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sey
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sim
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Spi
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Std
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Ste
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sto
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Such
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Sys
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Tym
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Uhl
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Van
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Vyucujici
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Web
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Zach
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Zah
import cz.jaro.rozvrh.rozvrh.Vjec.VyucujiciVjec.Zel
import kotlinx.serialization.Serializable

@Serializable
sealed interface Vjec {
    val jmeno: String
    val zkratka: String

    companion object {
        val tridy = listOf(
            Tridy,
            TridaVjec.A1,
            TridaVjec.E1,
            TridaVjec.S1,
            TridaVjec.A2,
            TridaVjec.E2,
            TridaVjec.S2,
            TridaVjec.A3,
            TridaVjec.E3,
            TridaVjec.S3,
            TridaVjec.A4,
            TridaVjec.E4,
            TridaVjec.S4,
            TridaVjec.E5,
            TridaVjec.E6,
            TridaVjec.E7,
            TridaVjec.E8
        )
        val mistnosti = listOf(
            Mistnosti,
            MistnostVjec.A1,
            MistnostVjec.E1,
            MistnostVjec.S1,
            MistnostVjec.A2,
            MistnostVjec.E2,
            MistnostVjec.S2,
            MistnostVjec.A3,
            MistnostVjec.E3,
            MistnostVjec.S3,
            MistnostVjec.A4,
            MistnostVjec.E4,
            MistnostVjec.S4,
            MistnostVjec.E5,
            MistnostVjec.E6,
            MistnostVjec.E7,
            MistnostVjec.E8,
            Aula,
            C1,
            C2,
            C3,
            Hv,
            Inf1,
            Inf2,
            LBi,
            LCh,
            Tv,
            Vv
        )
        val vyucujici = listOf(
            Vyucujici,
            Ada,
            Bah,
            Bart,
            Ben,
            Boc,
            Cer,
            Chyt,
            Coud,
            Cou,
            Dol,
            Dre,
            Dvk,
            Dvo,
            Fil,
            Gra,
            Hab,
            Hor,
            Hou,
            Hru,
            Hys,
            Jir,
            Glm,
            Kov,
            Kra,
            Krh,
            Kri1,
            Laf,
            Lud,
            Mik,
            Mus,
            Muk,
            Naj,
            Nec,
            Nova,
            Pau,
            Pet,
            Pfe,
            Pia,
            Pit,
            Pok,
            Rad,
            Reg,
            Ren,
            Roz,
            Sat,
            Scho,
            Sea,
            Sek,
            Sey,
            Sim,
            Spi,
            Ste,
            Std,
            Sto,
            Such,
            Sys,
            Tym,
            Uhl,
            Van,
            Web,
            Zach,
            Zah,
            Zel
        )
    }

    @Serializable
    sealed class TridaVjec(
        override val jmeno: String,
        val odkaz: String? = null,
    ) : Vjec {
        override val zkratka: String get() = jmeno

        @Serializable
        object Tridy : TridaVjec(jmeno = "Třídy")
        @Serializable
        object A1 : TridaVjec(jmeno = "1.A", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UW?TouchMode=1")
        @Serializable
        object E1 : TridaVjec(jmeno = "1.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UV?TouchMode=1")
        @Serializable
        object S1 : TridaVjec(jmeno = "1.S", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UX?TouchMode=1")
        @Serializable
        object A2 : TridaVjec(jmeno = "2.A", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UT?TouchMode=1")
        @Serializable
        object E2 : TridaVjec(jmeno = "2.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UU?TouchMode=1")
        @Serializable
        object S2 : TridaVjec(jmeno = "2.S", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/US?TouchMode=1")
        @Serializable
        object A3 : TridaVjec(jmeno = "3.A", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UP?TouchMode=1")
        @Serializable
        object E3 : TridaVjec(jmeno = "3.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UQ?TouchMode=1")
        @Serializable
        object S3 : TridaVjec(jmeno = "3.S", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UR?TouchMode=1")
        @Serializable
        object A4 : TridaVjec(jmeno = "4.A", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UM?TouchMode=1")
        @Serializable
        object E4 : TridaVjec(jmeno = "4.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UO?TouchMode=1")
        @Serializable
        object S4 : TridaVjec(jmeno = "4.S", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UN?TouchMode=1")
        @Serializable
        object E5 : TridaVjec(jmeno = "5.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UJ?TouchMode=1")
        @Serializable
        object E6 : TridaVjec(jmeno = "6.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UI?TouchMode=1")
        @Serializable
        object E7 : TridaVjec(jmeno = "7.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UC?TouchMode=1")
        @Serializable
        object E8 : TridaVjec(jmeno = "8.E", odkaz = "https://gymceska.bakalari.cz/timetable/public/###/class/UA?TouchMode=1")
    }

    @Serializable
    sealed class MistnostVjec(
        override val jmeno: String,
        val napoveda: String? = null,
    ) : Vjec {
        override val zkratka: String get() = jmeno

        @Serializable
        object Mistnosti : MistnostVjec(jmeno = "Místnosti")
        @Serializable
        object A1 : MistnostVjec(jmeno = "1.A", napoveda = " fyzika (1)")
        @Serializable
        object E1 : MistnostVjec(jmeno = "1.E", napoveda = " třída PO Týmalový (2)")
        @Serializable
        object S1 : MistnostVjec(jmeno = "1.S", napoveda = " smrdí jako pes (nejlepší z esek) (0)")
        @Serializable
        object A2 : MistnostVjec(jmeno = "2.A", napoveda = " za dveřma (1)")
        @Serializable
        object E2 : MistnostVjec(jmeno = "2.E", napoveda = " nad dveřma (2)")
        @Serializable
        object S2 : MistnostVjec(jmeno = "2.S", napoveda = " jedna z tich dvou (0)")
        @Serializable
        object A3 : MistnostVjec(jmeno = "3.A", napoveda = " děják (1)")
        @Serializable
        object E3 : MistnostVjec(jmeno = "3.E", napoveda = " třída s mrkví (1)")
        @Serializable
        object S3 : MistnostVjec(jmeno = "3.S", napoveda = " jedna z tich dvou (0)")
        @Serializable
        object A4 : MistnostVjec(jmeno = "4.A", napoveda = " vedle nás (2)")
        @Serializable
        object E4 : MistnostVjec(jmeno = "4.E", napoveda = " ta hnusná třída vedle fyziky (POZOR NA PRIMÁTY!) (1)")
        @Serializable
        object S4 : MistnostVjec(jmeno = "4.S", napoveda = " ta s tou černou tabulí (za rohem) (0)")
        @Serializable
        object E5 : MistnostVjec(jmeno = "5.E", napoveda = ", no, to snad víš")
        @Serializable
        object E6 : MistnostVjec(jmeno = "6.E", napoveda = " pod schodama (s blbou akustikou) (2)")
        @Serializable
        object E7 : MistnostVjec(jmeno = "7.E", napoveda = " chemie (poslanecká sněmovna) (2)")
        @Serializable
        object E8 : MistnostVjec(jmeno = "8.E", napoveda = " bižule (špenát) (2)")
        @Serializable
        object Aula : MistnostVjec(jmeno = "Aula", napoveda = " v aule (0)")
        @Serializable
        object C1 : MistnostVjec(jmeno = "C1", napoveda = " ta blíž ke žlutýmu (3)")
        @Serializable
        object C2 : MistnostVjec(jmeno = "C2", napoveda = " ta blíž hudebně (3)")
        @Serializable
        object C3 : MistnostVjec(jmeno = "C3", napoveda = " ta u kolatého stolu (3)")
        @Serializable
        object Hv : MistnostVjec(jmeno = "Hv", napoveda = " hudebna (3)")
        @Serializable
        object Inf1 : MistnostVjec(jmeno = "Inf1", napoveda = " ta nejlepší třída na škole (podle R) (1)")
        @Serializable
        object Inf2 : MistnostVjec(jmeno = "Inf2", napoveda = " ta nejužší učebna na škole (3)")
        @Serializable
        object LBi : MistnostVjec(jmeno = "LBi", napoveda = " vedle bižule (2)")
        @Serializable
        object LCh : MistnostVjec(jmeno = "LCh", napoveda = " za váhovnou (2)")
        @Serializable
        object Tv : MistnostVjec(jmeno = "Tv", napoveda = " na dvoře (-½)")
        @Serializable
        object Vv : MistnostVjec(jmeno = "Vv", napoveda = " věžička (4)")

        @Serializable
        object Mim : MistnostVjec(jmeno = "mim", napoveda = " venku (-½)")
    }


    @Serializable
    sealed class VyucujiciVjec(
        override val jmeno: String,
        override val zkratka: String,
    ) : Vjec {

        @Serializable
        object Vyucujici : VyucujiciVjec(zkratka = "", jmeno = "Vyučující")
        @Serializable
        object Ada : VyucujiciVjec(zkratka = "Ada", jmeno = "Adamcová Eva")
        @Serializable
        object Bah : VyucujiciVjec(zkratka = "Bah", jmeno = "Bahenský Petr")
        @Serializable
        object Bart : VyucujiciVjec(zkratka = "Bart", jmeno = "Bártů Jana")
        @Serializable
        object Ben : VyucujiciVjec(zkratka = "Ben", jmeno = "Beníšek Petr")
        @Serializable
        object Boc : VyucujiciVjec(zkratka = "Boc", jmeno = "Boček Lukáš")
        @Serializable
        object Cer : VyucujiciVjec(zkratka = "Cer", jmeno = "Černá Marie")
        @Serializable
        object Chyt : VyucujiciVjec(zkratka = "Chyt", jmeno = "Chytil Alena")
        @Serializable
        object Coud : VyucujiciVjec(zkratka = "Čou", jmeno = "Čoudková Štěpánka")
        @Serializable
        object Cou : VyucujiciVjec(zkratka = "Cou", jmeno = "Couf Jiří")
        @Serializable
        object Dol : VyucujiciVjec(zkratka = "Dol", jmeno = "Dolejší Petr")
        @Serializable
        object Dre : VyucujiciVjec(zkratka = "Dře", jmeno = "Dřevikovská Pavla")
        @Serializable
        object Dvk : VyucujiciVjec(zkratka = "Dvk", jmeno = "Dvořák Jan T")
        @Serializable
        object Dvo : VyucujiciVjec(zkratka = "Dvo", jmeno = "Dvořka Jan")
        @Serializable
        object Fil : VyucujiciVjec(zkratka = "Fil", jmeno = "Filip Jiří")
        @Serializable
        object Gra : VyucujiciVjec(zkratka = "Gra", jmeno = "Graman Miroslav")
        @Serializable
        object Hab : VyucujiciVjec(zkratka = "Hab", jmeno = "Habi Petra")
        @Serializable
        object Hor : VyucujiciVjec(zkratka = "Hor", jmeno = "Hornátová Lucie")
        @Serializable
        object Hou : VyucujiciVjec(zkratka = "Hou", jmeno = "Houšková Pavlína")
        @Serializable
        object Hru : VyucujiciVjec(zkratka = "Hru", jmeno = "Hrubešová Radka")
        @Serializable
        object Hys : VyucujiciVjec(zkratka = "Hys", jmeno = "Hýsek Miroslav")
        @Serializable
        object Jir : VyucujiciVjec(zkratka = "Jir", jmeno = "Jirsová Žaneta")
        @Serializable
        object Glm : VyucujiciVjec(zkratka = "Kot", jmeno = "Kotlas Glum Miroslav")
        @Serializable
        object Kov : VyucujiciVjec(zkratka = "Kov", jmeno = "Kovaříková Evička")
        @Serializable
        object Kra : VyucujiciVjec(zkratka = "Kra", jmeno = "Kratochvílová Tereza")
        @Serializable
        object Krh : VyucujiciVjec(zkratka = "Krh", jmeno = "Krhounková Věra")
        @Serializable
        object Kri1 : VyucujiciVjec(zkratka = "Kří1", jmeno = "Křížovka Radka")
        @Serializable
        object Laf : VyucujiciVjec(zkratka = "Laf", jmeno = "Lafata David")
        @Serializable
        object Lud : VyucujiciVjec(zkratka = "Lud", jmeno = "Ludvíková Klára")
        @Serializable
        object Mik : VyucujiciVjec(zkratka = "Mik", jmeno = "Mikeš Vladimír")
        @Serializable
        object Mus : VyucujiciVjec(zkratka = "Mus", jmeno = "Musilka Hana")
        @Serializable
        object Muk : VyucujiciVjec(zkratka = "Muk", jmeno = "Mušková Marie")
        @Serializable
        object Naj : VyucujiciVjec(zkratka = "Naj", jmeno = "Najbrt Tomáš")
        @Serializable
        object Nec : VyucujiciVjec(zkratka = "Neč", jmeno = "Nečilová Lenka")
        @Serializable
        object Nova : VyucujiciVjec(zkratka = "Nova", jmeno = "Nová Petra")
        @Serializable
        object Pau : VyucujiciVjec(zkratka = "Pau", jmeno = "Paukert Blb Roman")
        @Serializable
        object Pet : VyucujiciVjec(zkratka = "Pet", jmeno = "Petřeková Jaroslava")
        @Serializable
        object Pfe : VyucujiciVjec(zkratka = "Pfe", jmeno = "Pfefrčková Michala")
        @Serializable
        object Pia : VyucujiciVjec(zkratka = "Pia", jmeno = "Píša Adam č")
        @Serializable
        object Pit : VyucujiciVjec(zkratka = "Pit", jmeno = "Ester")
        @Serializable
        object Pok : VyucujiciVjec(zkratka = "Pok", jmeno = "Pokorná Ivana")
        @Serializable
        object Rad : VyucujiciVjec(zkratka = "Rad", jmeno = "_")
        @Serializable
        object Reg : VyucujiciVjec(zkratka = "Reg", jmeno = "Regulová Věra")
        @Serializable
        object Ren : VyucujiciVjec(zkratka = "Ren", jmeno = "Řezníčková Zdeňka")
        @Serializable
        object Roz : VyucujiciVjec(zkratka = "Roz", jmeno = "Rožboud Stanislav")
        @Serializable
        object Sat : VyucujiciVjec(zkratka = "Šat", jmeno = "Šátaviga Tereza")
        @Serializable
        object Scho : VyucujiciVjec(zkratka = "Scho", jmeno = "Schönová Alena")
        @Serializable
        object Sea : VyucujiciVjec(zkratka = "Sea", jmeno = "Sekyrka Antonín")
        @Serializable
        object Sek : VyucujiciVjec(zkratka = "Sek", jmeno = "Sekyrka Vlastimil")
        @Serializable
        object Sey : VyucujiciVjec(zkratka = "Sey", jmeno = "Sekyrková Miroslava")
        @Serializable
        object Sim : VyucujiciVjec(zkratka = "Šim", jmeno = "Šimonek Milan")
        @Serializable
        object Spi : VyucujiciVjec(zkratka = "Špi", jmeno = "Špišáková Petra")
        @Serializable
        object Ste : VyucujiciVjec(zkratka = "Šte", jmeno = "Štěpánková Hana")
        @Serializable
        object Std : VyucujiciVjec(zkratka = "Štd", jmeno = "Štoudek Ajfel Martin")
        @Serializable
        object Sto : VyucujiciVjec(zkratka = "Što", jmeno = "Štoudková Vladislava")
        @Serializable
        object Such : VyucujiciVjec(zkratka = "Such", jmeno = "Suchý Jan")
        @Serializable
        object Sys : VyucujiciVjec(zkratka = "Sys", jmeno = "Syslová Zuzana")
        @Serializable
        object Tym : VyucujiciVjec(zkratka = "Tym", jmeno = "Týmalová Simona")
        @Serializable
        object Uhl : VyucujiciVjec(zkratka = "Uhl", jmeno = "Uhlík Michal")
        @Serializable
        object Van : VyucujiciVjec(zkratka = "Van", jmeno = "Vaníček Miroslav")
        @Serializable
        object Web : VyucujiciVjec(zkratka = "Web", jmeno = "Weber Oldřich")
        @Serializable
        object Zach : VyucujiciVjec(zkratka = "Zach", jmeno = "Zach Vojtěch")
        @Serializable
        object Zah : VyucujiciVjec(zkratka = "Zah", jmeno = "Zahradníčková Zita")
        @Serializable
        object Zel : VyucujiciVjec(zkratka = "Zel", jmeno = "Zelenka Petj")
    }
}
