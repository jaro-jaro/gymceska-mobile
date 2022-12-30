package cz.jaro.rozvrh.suplovani

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.rozvrh.Seznamy
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SuplovaniFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_suplovani, container, false)

        val spSuplTrida = view.findViewById<Spinner>(R.id.spSuplTrida)

        spSuplTrida.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, Seznamy.tridy)

        val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

        spSuplTrida.setSelection(sp.getInt("sva_trida_index", 13))
        aktualizovat(view)

        spSuplTrida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view2: View?,
                position: Int,
                id: Long
            ) {
                aktualizovat(view)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return  view
    }



    private fun aktualizovat(view: View) {


        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val ll = LinearLayout(requireContext())
            val pb = ProgressBar(requireContext())
            pb.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            pb.updateLayoutParams<LinearLayout.LayoutParams> {
                setMargins(24, 24, 0, 24)
            }
            ll.addView(pb)

            setTitle(getString(R.string.nacitani))
            setView(ll)
            setCancelable(false)
        }.create()
        dialog.show()

        val tvSupl = view.findViewById<TextView>(R.id.tvSupl)
        val spSuplTrida = view.findViewById<Spinner>(R.id.spSuplTrida)
        val tvWarn = view.findViewById<TextView>(R.id.tvWarn2)

        tvWarn.text = ""

        if (spSuplTrida.selectedItemPosition == 0) {
            tvSupl.text = ""
            return
        }

        Thread {
            val doc: Document?


            val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

            if (isOnline()) {
                doc = Jsoup.connect("https://www.gymceska.cz/rozvrhy/suplobec.htm").get()

                sp.edit().apply {
                    putString("supl", doc.toString())

                    val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))

                    putString("supl_datum", formatter.format(Date()))

                    apply()
                }



            } else {

                val html = sp.getString("supl", null) ?: run {

                    dialog.cancel()

                    Toast.makeText(requireActivity(), R.string.neni_stazen, Toast.LENGTH_LONG).show()

                    return@Thread
                }

                val datum = sp.getString("supl_datum", "")

                tvWarn.text = getString(R.string.datum, datum)

                doc = Jsoup.parse(html)
            }

            doc ?: return@Thread

            val dny = doc.getElementsByTag("p")
                .filter { e-> e.className() == "textlarge_3" }
                .map { "<br /><b>" + it.text().replace("Suplování: ", "") + ":</b><br />" }

            var vytahovat = false

            val vejcuc = mutableListOf<MutableList<String>>()

            for ((den, table) in doc.getElementsByClass("tb_supltrid_3").withIndex()) {
                vejcuc.add(mutableListOf(dny[den]))

                for (radek in table.getElementsByTag("tr")) {
                    if (radek.className() == "tr_supltrid_3") continue

                    for ((i, bunka) in radek.getElementsByTag("td").withIndex()) {
                        if (i == 0) {
                            Log.i("bunka", bunka.text() + "_")
                            vytahovat =
                                bunka.text().contains(spSuplTrida.selectedItem.toString())
                                    || (vytahovat && bunka.text().isBlank())
                        }

                        if (vytahovat) {
                            when (i) {
                                0 -> {
                                    vejcuc.add(mutableListOf())
                                }
                                1 -> {
                                    vejcuc[vejcuc.lastIndex].add(bunka.text())
                                }
                                3 -> {
                                    if (bunka.text().isNotBlank())
                                        vejcuc[vejcuc.lastIndex].add(" " + bunka.text())
                                    vejcuc[vejcuc.lastIndex].add(" – ")
                                }
                                in listOf(4, 7) -> {
                                    if (bunka.text().isBlank()) continue
                                    vejcuc[vejcuc.lastIndex].add(" " + bunka.text())
                                }
                                in listOf(2, 5) -> {
                                    if (bunka.text().isBlank()) continue
                                    vejcuc[vejcuc.lastIndex].add(" – " + bunka.text())
                                }
                                6 -> {
                                    if (bunka.text().isBlank()) continue
                                    vejcuc[vejcuc.lastIndex].add(
                                        vejcuc.last().lastIndex - 1,
                                        " " + bunka.text()
                                    )
                                }
                            }
                        }
                    }
                }

                if (vejcuc.last() == mutableListOf(dny[den])) {
                    vejcuc.add(mutableListOf())
                }
            }


            requireActivity().runOnUiThread {
                tvSupl.text = Html.fromHtml(
                    vejcuc.joinToString("<br />") {

                        if (it.isEmpty()) {
                            "Žádné suplování"
                        } else {
                            it.joinToString("").replace(" –  – ", " – ").replace("  ", " ")
                        }
                    }, 0
                )

                dialog.cancel()
            }

        }.start()
    }


    private fun isOnline(): Boolean {
        val connManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }


}
