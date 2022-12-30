package cz.jaro.rozvrh.ukoly

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify.ALL
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.FirebaseApp
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
import java.util.Calendar
import java.util.Date
import java.util.Locale


class UkolyFragment : Fragment() {

    // to tu musí bejt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ukoly_menu, menu)
    }
    private lateinit var binding: FragmentUkolyBinding
    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUkolyBinding.inflate(inflater, container, false)
        return binding.root
    }

    //menu
    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

        when (item.itemId) {
            R.id.actionSpravovat -> {

                MaterialAlertDialogBuilder(requireContext()).apply {

                    val et = EditText(context)

                    setTitle(getString(R.string.nacitani))
                    setView(et)

                    setPositiveButton("Heslo?") { dialog, _ ->

                        if (et.text.toString() == "super tajné heslo ⨀_⨀") {
                            val fragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
                            fragment.navController.navigate(R.id.action_ukolyFragment_to_spravceUkoluFragment)
                        }
                        dialog.cancel()
                    }
                    show()
                }
            }
            R.id.actionNastaveniOznameni -> {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.nastaveni_oznameni)

                    val clNastaveniOznameni = LayoutInflater.from(context).inflate(R.layout.nastaveni_oznameni, null)

                    setView(clNastaveniOznameni)

                    val tpOznameni = clNastaveniOznameni.findViewById<TimePicker>(R.id.tpOznameni)
                    val swDostavatOznameni = clNastaveniOznameni.findViewById<SwitchMaterial>(R.id.swDostavatOznameni)

                    tpOznameni.setIs24HourView(true)

                    val cas = sp.getString("oznameni_cas", "15:00")
                    val dostavat = sp.getBoolean("oznameni", false)

                    tpOznameni.hour = cas!!.split(":")[0].toInt()
                    tpOznameni.minute = cas.split(":")[1].toInt()

                    swDostavatOznameni.isChecked = dostavat

                    setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.cancel()

                        sp.edit {
                            putString("oznameni_cas", "${tpOznameni.hour}:${tpOznameni.minute}")
                            putBoolean("oznameni", swDostavatOznameni.isChecked)
                        }

                        Log.d("ozn", swDostavatOznameni.isChecked.toString())

                        if (!swDostavatOznameni.isChecked) return@setPositiveButton

                        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val oldIntent = Intent(requireContext(), UkolyReceiver::class.java)
                        val oldPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, oldIntent, PendingIntent.FLAG_IMMUTABLE)

                        alarmManager.cancel(oldPendingIntent)

                        val calendar: Calendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, tpOznameni.hour)
                            set(Calendar.MINUTE, tpOznameni.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val intent = Intent(requireContext(), UkolyReceiver::class.java)

                        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)


                        Log.d("ozn", "naplanovano")
                        Log.d("ozn", calendar.toString())
                    }

                    setNegativeButton(R.string.zrusit) { dialog, _ -> dialog.cancel() }

                    show()
                }
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // nacitani
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val ll = LinearLayout(requireContext())
            val pb = ProgressBar(requireContext())
            pb.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            pb.updateLayoutParams<LinearLayout.LayoutParams> {
                setMargins(24, 24, 0, 24)
            }
            ll.addView(pb)

            setTitle(getString(R.string.nacitani))
            setView(ll)
            setCancelable(false)
        }.create()
        dialog.show()

        //init
        FirebaseApp.initializeApp(requireContext())
        val database = FirebaseDatabase.getInstance(Firebase.app, "https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/")
        val myRef = database.getReference("ukoly5")

        val sp = requireActivity().getSharedPreferences("hm", Context.MODE_PRIVATE)

        if (isOnline()) {
            myRef.addValueEventListener(object : ValueEventListener {
                // az se nactou data z databaze
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dialog.cancel()

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
                    dialog.cancel()
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
