package com.aditya.isiquest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate

class Blk(val m: Int, val d: String)
class Day(val t: String, val b: List<Blk>)

private val PLAN = listOf(
"Counting basics|90~Read Chen–Koh Ch.1 (perm and comb);60~Worked examples;90~Basic counting problems",
"Circular perms, multisets|60~Review circular perms and multisets;60~UGA speed drill;90~P and C problems (Excursion)",
"Binomial identities|90~Chen–Koh Ch.2 (binomials, Pascal);60~UGA identity recognition;90~Prove identities, both ways",
"Pigeonhole principle|90~Chen–Koh Ch.3 (PHP);60~Objective PHP drills;90~Basic and generalized PHP uses",
"ISI counting, first proofs|90~Objective counting set (ISI 🍅);120~Basic UGB combinatorial proofs",
"Weak spots, mixed sets|60~Review weak concepts;150~Mixed combinatorics (Excursion)",
"CHECKPOINT 1: Combinatorics|90~UGA speed practice (timed);120~UGB proof writing (timed)",
"Division algorithm, GCD|90~Burton Ch.2 (division, GCD);60~Euclidean reversal drills;90~Formal divisibility proofs",
"Bézout, coprimality|90~Bézout and linear combinations (Excursion);60~UGA GCD/LCM drills;90~GCD and coprimality UGB",
"Primes and FTA|90~Burton Ch.3 (primes, FTA);60~Factorization shortcuts;90~Prime distribution, divisor counts",
"Congruences|90~Burton Ch.4 (congruences);60~Linear congruence drills;90~Modular reduction, residue classes",
"ISI NT, divisibility proofs|90~Objective NT set (ISI 🍅);120~Draft UGB divisibility proofs",
"Congruence review|60~Review congruence properties;150~Mixed NT sets (Excursion)",
"CHECKPOINT 2: Number theory|90~UGA speed practice (timed);120~UGB modular proofs (timed)",
"Inclusion-exclusion|90~Chen–Koh Ch.4 (PIE);60~Set-intersection drills;90~Restricted counting proofs",
"Derangements|90~Derangements, fixed points;60~D_n evaluations;90~Advanced PIE (Excursion)",
"Invariants|90~Invariants and monovariance (Excursion);60~Spot the invariant;90~Grid, coloring, game proofs",
"Recurrences|90~Chen–Koh Ch.5 (characteristic eqs);60~Characteristic equation drills;90~System modeling proofs",
"Non-homogeneous, GFs|90~Non-homogeneous recurrences, intro GFs;60~Objective recurrences (ISI 🍅);90~Closed-form derivations",
"Recurrence proof grind|60~Consolidated review;180~Hard proofs, Chen–Koh Ch.5",
"CHECKPOINT 3: Advanced counting|90~UGA advanced counting (timed);150~UGB recurrences, invariants (timed)",
"FLT and Wilson|90~Burton Ch.5 (FLT, Wilson);60~High-power exponent drills;90~Large power congruences, primality",
"Euler’s phi|90~Burton Ch.7 (phi, Euler);60~phi(n) drills;90~Totient proofs, residue systems",
"Linear Diophantine|90~Burton 2.5 (Diophantine);60~Parametric solution drills;90~Solve via Euclid reversal",
"Arithmetic functions|90~tau, sigma, mu (Excursion);60~Multiplicative sum drills;90~Divisor, multiplicative proofs",
"ISI NT, Diophantine proofs|90~Objective NT set (ISI 🍅);120~Draft Diophantine and prime proofs",
"Formula review, past proofs|60~Formula review;180~Mixed UGB from past ISI papers",
"FINAL BOSS: Month 1 mock|120~UGA mock (combinatorics and NT);120~UGB mock (combinatorics and NT)")

class MainActivity : Activity() {
    private val days = PLAN.map { r ->
        val (t, b) = r.split("|")
        Day(t, b.split(";").map { x -> val (m, d) = x.split("~"); Blk(m.toInt(), d) })
    }
    private lateinit var p: SharedPreferences
    private lateinit var root: LinearLayout
    private val h = Handler(Looper.getMainLooper())
    private var clock: TextView? = null
    private var vd = -1

    private val loop = object : Runnable {
        override fun run() {
            val end = p.getLong("end", 0)
            val now = System.currentTimeMillis()
            if (end > 0 && now >= end) {            // block finished: credit it
                val k = p.getString("blk", "") ?: ""
                if (k.isNotEmpty()) p.edit().putBoolean(k, true).apply()
                p.edit().putString("blk", "").putLong("end", 0).apply()
                render()
            } else if (end > now) clock?.text = fmt((end - now) / 1000)
            h.postDelayed(this, 1000)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        p = getSharedPreferences("q", MODE_PRIVATE)
        if (!p.contains("start")) p.edit().putLong("start", LocalDate.now().toEpochDay()).apply()
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 120, 40, 80) }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() { super.onResume(); render(); h.post(loop) }
    override fun onPause() { super.onPause(); h.removeCallbacks(loop) }

    private fun fmt(s: Long) = "%d:%02d:%02d".format(s / 3600, s % 3600 / 60, s % 60)
    private fun tv(s: String, sz: Float, bold: Boolean) = TextView(this).apply {
        text = s; textSize = sz; setPadding(0, 12, 0, 4)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }
    private fun btn(s: String, f: () -> Unit) = Button(this).apply { text = s; setOnClickListener { f() } }
    private fun done(i: Int, j: Int) = p.getBoolean("b_${i}_$j", false)
    private fun today() = (LocalDate.now().toEpochDay() - p.getLong("start", 0)).toInt()
    private fun unlocked(): Int { var w = 0; while (w < 3 && p.getBoolean("c_$w", false)) w++; return w }
    private fun start(i: Int, j: Int) {
        p.edit().putString("blk", "b_${i}_$j")
            .putLong("end", System.currentTimeMillis() + days[i].b[j].m * 60000L).apply()
        render()
    }

    private fun render() {
        root.removeAllViews(); clock = null
        val t = today(); val uw = unlocked()
        var xp = 0
        days.forEachIndexed { i, d -> d.b.forEachIndexed { j, b -> if (done(i, j)) xp += b.m } }
        root.addView(tv("ISI Month 1: Checkpoint Quest", 20f, true))
        root.addView(tv("XP $xp    leaks ${p.getInt("leaks", 0)}    checkpoints ${(0..3).count { p.getBoolean("c_$it", false) }}/4", 14f, false))
        val end = p.getLong("end", 0); val now = System.currentTimeMillis()
        if (end > now) {
            root.addView(tv("FOCUS BLOCK RUNNING. Study now. Phone down.", 14f, true))
            clock = tv(fmt((end - now) / 1000), 48f, true); root.addView(clock)
            root.addView(btn("Give up (no XP, +1 leak)") {
                AlertDialog.Builder(this).setMessage("Abandon this block? You get no XP for it.")
                    .setPositiveButton("Give up") { _, _ ->
                        p.edit().putInt("leaks", p.getInt("leaks", 0) + 1).putString("blk", "").putLong("end", 0).apply(); render()
                    }.setNegativeButton("Keep going", null).show()
            })
            return
        }
        if (vd < 0) vd = minOf(maxOf(t, 0), days.size - 1)
        val d = days[vd]
        root.addView(tv("Day ${vd + 1} of 28: ${d.t}", 17f, true))
        root.addView(tv("${LocalDate.ofEpochDay(p.getLong("start", 0) + vd)}   week ${vd / 7 + 1}", 13f, false))
        val ok = vd <= t && vd / 7 <= uw
        if (!ok) root.addView(tv(if (vd > t) "Future day. No pre-banking." else "Locked: clear the previous checkpoint.", 14f, false))
        d.b.forEachIndexed { j, b ->
            if (done(vd, j)) root.addView(tv("✓ ${b.d} (${b.m} min)", 15f, false))
            else root.addView(btn("${b.d} (${b.m} min)${if (ok) "" else " [locked]"}") { if (ok) start(vd, j) })
        }
        if (vd % 7 == 6 && d.b.indices.all { done(vd, it) } && !p.getBoolean("c_${vd / 7}", false) && vd / 7 == uw)
            root.addView(btn("Clear checkpoint ${vd / 7 + 1}") {
                AlertDialog.Builder(this)
                    .setMessage("Error log done for every lost mark (conceptual gap, execution slip, proof failure), each one re-solved?")
                    .setPositiveButton("Yes, done") { _, _ -> p.edit().putBoolean("c_${vd / 7}", true).apply(); render() }
                    .setNegativeButton("Not yet", null).show()
            })
        root.addView(btn("◀ Previous day") { if (vd > 0) { vd--; render() } })
        root.addView(btn("Next day ▶") { if (vd < days.size - 1) { vd++; render() } })
        val sb = StringBuilder()
        for (i in days.indices) {
            val dn = days[i].b.indices.count { done(i, it) }
            sb.append(
                if (dn == days[i].b.size) "✅" else if (i / 7 > uw) "🔒" else if (dn > 0) "🟨"
                else if (i < t) "❌" else if (i == t) "▶️" else "⬜"
            )
            if (i % 7 == 6) sb.append("\n")
        }
        root.addView(tv("Map (✅ done, 🟨 partial, ❌ missed, ▶️ today, 🔒 locked)", 13f, false))
        root.addView(tv(sb.toString(), 22f, false))
        root.addView(btn("Restart quest from today") {
            AlertDialog.Builder(this).setMessage("Erase all progress?").setPositiveButton("Erase") { _, _ ->
                p.edit().clear().putLong("start", LocalDate.now().toEpochDay()).apply(); vd = -1; render()
            }.setNegativeButton("Cancel", null).show()
        })
    }
}
