package cz.jaro.rozvrh.ukoly

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify.ALL
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import cz.jaro.rozvrh.R
import cz.jaro.rozvrh.databinding.FragmentUkolyBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UkolyFragment : Fragment() {

    private lateinit var binding: FragmentUkolyBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FirebaseDatabase.getInstance(Firebase.app, "https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/")
        val myRef = database.getReference("ukoly5")

        val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

        if (isOnline()) {
            myRef.addValueEventListener(object : ValueEventListener {
                // az se nactou data z databaze
                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    dialog.cancel()

                    // kdyby se neco pokazilo
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return // !!!!!

                    // nemusime na nic varovat
                    binding.tvWarn3.text = ""

                    // stahnout ukoly
                    val ukoly = dataSnapshot.getValue<List<Map<String, String>>>() ?: return // !!!

                    // zformatovat ukoly a zobrazit je

                    vytvoritUkoly(ukoly.map {
                        requireActivity().getString(
                            R.string.ukol,
                            it["datum"],
                            it["predmet"],
                            it["nazev"]
                        )
                    })

                    sp.edit {
                        // ulozit ukoly do zarizeni
                        putString("ukoly",
                            ukoly.joinToString("<br />") { ukol ->
                                requireActivity().getString(
                                    R.string.ukol,
                                    ukol["datum"],
                                    ukol["predmet"],
                                    ukol["nazev"]
                                )
                            }
                        )

                        // zaznamenat datum ulozeni
                        val formatter = SimpleDateFormat("dd. MM. yyyy", Locale("cs"))
                        putString("ukoly_datum", formatter.format(Date()))

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("hm", "Hmmmm", error.toException())
//                    dialog.cancel()
                }
            })
        } else { // offline
            // najit ukoly v zarizeni
            val ukoly = sp.getString("ukoly", null)

            // pokud tam nejsou, nic nenacitat
            ukoly ?: run {
                Toast.makeText(requireActivity(), R.string.neni_stazeno, Toast.LENGTH_LONG).show()

                return
            }

            // mame ukoly!

            // zobrazit varovani na stara data
            val datum = sp.getString("ukoly_datum", "")
            binding.tvWarn3.text = getString(R.string.datum, datum)

            // zobrazit ukoly

            vytvoritUkoly(ukoly.split("<br />"))
        }
    }

    private fun vytvoritUkoly(ukoly: List<String>) {

        val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

        val puvodniSkrtleUkoly = sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toMutableSet()

        for (i in ukoly.indices) {

            var ukol = ukoly[i]

            val tv = TextView(requireContext())

            if (ukol in puvodniSkrtleUkoly) {
                puvodniSkrtleUkoly.remove(ukol)
                ukol = "<strike>${ukol}</strike>"
            }

            tv.text = Html.fromHtml(ukol, 0)

            binding.llUkoly.addView(tv, MATCH_PARENT, WRAP_CONTENT)

            val l = {
                Log.d("hm", sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toString())
                val skrtleUkoly = sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toMutableSet()

                Log.d("ukol", ukol)

                val ukolBezStriku = ukol.removePrefix("<strike>").removeSuffix("</strike>")

                ukol =
                    if (ukolBezStriku in skrtleUkoly) {
                        skrtleUkoly.remove(ukolBezStriku)
                        ukol.removePrefix("<strike>").removeSuffix("</strike>")
                    } else {
                        skrtleUkoly += ukol // tou dobou jeste strike nema -> nemusime ho davat bez striku
                        "<strike>${ukol}</strike>"
                    }

                tv.text = Html.fromHtml(ukol, 0)

                LinkifyCompat.addLinks(tv, ALL)

                sp.edit {
                    putStringSet("skrtle_ukoly", skrtleUkoly)
                }
                Log.d("hm", sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toString())

            }

            if(ukol.contains("http://") || ukol.contains("https://")) {
                tv.setOnLongClickListener {
                    l()

                    return@setOnLongClickListener true
                }

                LinkifyCompat.addLinks(tv, ALL)
            } else {
                tv.setOnClickListener {
                    l()
                }
            }


        }

        val skrtleUkoly = sp.getStringSet("skrtle_ukoly", mutableSetOf())!!.toMutableSet()

        skrtleUkoly.removeAll(puvodniSkrtleUkoly)

        sp.edit {
            putStringSet("skrtle_ukoly", skrtleUkoly)
        }
    }

    private fun isOnline(): Boolean {
        val connManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

}
