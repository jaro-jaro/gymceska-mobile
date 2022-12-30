package cz.jaro.rozvrh.ukoly

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import cz.jaro.rozvrh.R


class UkolyAdapter(var seznam: List<MutableMap<String, String>>, private val activity: Activity) : RecyclerView.Adapter<UkolyAdapter.ViewHolder>() {

    private var recyclerView: RecyclerView? =  null

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val tvUkol = view.findViewById<TextView>(R.id.tvUkol)!!
        val btnOdstranit = view.findViewById<Button>(R.id.btnOdstranit)!!
        val btnUpravit = view.findViewById<Button>(R.id.btnUpravit)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        val view = LayoutInflater.from(parent.context).inflate(R.layout.ukol, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        seznam[pos].also { map ->
            holder.tvUkol.text = activity.getString(R.string.ukol, map["datum"], map["predmet"], map["nazev"])
        }

        holder.btnOdstranit.setOnClickListener {
            val position = holder.adapterPosition
            val a = seznam.toMutableList()
            a.removeAt(position)
            seznam = a
            ulozitAZaktualizovat()
        }

        holder.btnUpravit.setOnClickListener {
            val position = holder.adapterPosition

            MaterialAlertDialogBuilder(activity).apply {
                setTitle(R.string.upravit_ukol)

                val etDatum = EditText(activity).apply {
                    setText(seznam[position]["datum"])
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                }
                val etPredmet = EditText(activity).apply {
                    setText(seznam[position]["predmet"])
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                }
                val etNazev = EditText(activity).apply {
                    setText(seznam[position]["nazev"])
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                }

                setView(LinearLayout(activity).apply {
                    orientation = VERTICAL

                    addView(etDatum)
                    addView(etPredmet)
                    addView(etNazev)
                })
                setCancelable(false)

                setNegativeButton(R.string.zrusit) { dialog, _ ->
                    dialog.cancel()
                }

                setPositiveButton(android.R.string.ok) { dialog, _ ->

                    seznam = seznam.toMutableList().apply {
                        this[position] = mutableMapOf(
                            "datum" to etDatum.text.toString(),
                            "predmet" to etPredmet.text.toString(),
                            "nazev" to etNazev.text.toString(),
                        )
                    }.toList()

                    ulozitAZaktualizovat()

                    dialog.cancel()
                }
                show()
            }
        }
    }

    override fun getItemCount(): Int = seznam.size

    private val mesice =
        listOf("Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec")

    var minulejSeznam = listOf<Map<String, String>>()

    fun ulozitAZaktualizovat() {

        seznam = seznam.sortedBy { map ->
            val x = map["datum"]!!.replace("<b>", "").replace("</b>", "")
                .replace(" ", "").split(".")

            val y = if (x[0] in mesice) {
                (mesice.indexOf(x[0]) + 1) * 100
            } else {
                var x2 = x.map { it.toIntOrNull() }
                if (x2.size < 2) { x2 = listOf(0, 0) }
                x2[0] ?: run { x2 = listOf(0, 0) }
                x2[1] ?: run { x2 = listOf(0, 0) }
                x2[0]!! + x2[1]!! * 100
            }

            val z = (y + 600) % 1200
            if (z == 0) {
                1200
            } else {
                z
            }
        }

        val database = Firebase.database("https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/") // !!!
        val myRef = database.getReference("ukoly5")

        myRef.removeValue { _, _ ->
            myRef.setValue(seznam)
        }

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = minulejSeznam.size
            override fun getNewListSize(): Int = seznam.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = minulejSeznam[oldItemPosition] == seznam[newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = minulejSeznam[oldItemPosition] == seznam[newItemPosition]
        }, true).dispatchUpdatesTo(this)

        //notifyDataSetChanged()

        minulejSeznam = seznam
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }


}
