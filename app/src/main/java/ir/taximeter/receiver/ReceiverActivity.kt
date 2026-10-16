package ir.taximeter.receiver

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Test-only receiver. Pressing Approve only simulates a successful response; no money is charged. */
class ReceiverActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var requestView: TextView
    private lateinit var approve: Button
    private lateinit var decline: Button
    @Volatile private var pending: Pending? = null
    @Volatile private var running = false
    private var server: BluetoothServerSocket? = null

    private data class Pending(val id: String, val amount: Long, val trip: String, val latch: CountDownLatch = CountDownLatch(1), @Volatile var decision: String? = null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 28, 28, 28); gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(TextView(this).apply { text = "گیرندهٔ آزمایشی TaxiMeter"; textSize = 23f; gravity = Gravity.CENTER })
        status = TextView(this).apply { text = "در حال آماده‌سازی بلوتوث…"; textSize = 17f; setPadding(0, 24, 0, 24) }
        root.addView(status)
        requestView = TextView(this).apply { text = "هنوز مبلغی دریافت نشده است."; textSize = 20f; setPadding(0, 18, 0, 18) }
        root.addView(requestView)
        approve = Button(this).apply { text = "شبیه‌سازی پرداخت موفق"; isEnabled = false; setOnClickListener { decide("APPROVED") } }
        decline = Button(this).apply { text = "شبیه‌سازی پرداخت ناموفق"; isEnabled = false; setOnClickListener { decide("DECLINED") } }
        root.addView(approve); root.addView(decline)
        root.addView(TextView(this).apply { text = "هشدار: این برنامه فقط پاسخ آزمایشی تولید می‌کند؛ هیچ تراکنش بانکی انجام نمی‌شود."; textSize = 15f; setPadding(0, 28, 0, 0) })
        setContentView(ScrollView(this).apply { addView(root) })
        startServer()
    }

    private fun startServer() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) { status.text = "این گوشی بلوتوث ندارد."; return }
        if (!adapter.isEnabled) { status.text = "بلوتوث را از تنظیمات گوشی روشن کنید، سپس این برنامه را دوباره باز کنید."; return }
        running = true
        status.text = "آمادهٔ دریافت؛ گوشی راننده را با این گوشی Pair کنید. برنامه را باز نگه دارید."
        Thread {
            try {
                server = adapter.listenUsingRfcommWithServiceRecord("TaxiMeterTestReceiver", SERVICE_UUID)
                while (running) {
                    val socket = server?.accept() ?: break
                    handle(socket)
                }
            } catch (e: Exception) {
                if (running) runOnUiThread { status.text = "خطای بلوتوث: ${e.message ?: e.javaClass.simpleName}" }
            }
        }.start()
    }

    private fun handle(socket: BluetoothSocket) {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.inputStream, Charsets.UTF_8))
                val writer = OutputStreamWriter(s.outputStream, Charsets.UTF_8)
                val line = reader.readLine() ?: return
                val f = line.split('|')
                if (f.size < 4 || f[0] != "PAY") {
                    writer.write("RESULT|unknown|0|DECLINED|INVALID_REQUEST\n"); writer.flush(); return
                }
                val amount = f[2].toLongOrNull()
                if (amount == null || amount <= 0L) {
                    writer.write("RESULT|${f[1]}|${f.getOrElse(2) { "0" }}|DECLINED|INVALID_AMOUNT\n"); writer.flush(); return
                }
                val item = Pending(f[1], amount, f[3])
                pending = item
                runOnUiThread {
                    status.text = "درخواست دریافت شد؛ تصمیم آزمایشی را انتخاب کنید."
                    requestView.text = "مبلغ: ${amount} تومان\nشناسه: ${item.id}\nشناسه سفر: ${item.trip.ifBlank { "—" }}"
                    approve.isEnabled = true; decline.isEnabled = true
                }
                val answered = item.latch.await(120, TimeUnit.SECONDS)
                val decision = if (answered) item.decision ?: "DECLINED" else "DECLINED"
                val ref = if (decision == "APPROVED") "SIM-${System.currentTimeMillis()}" else "USER_DECLINED_OR_TIMEOUT"
                writer.write("RESULT|${item.id}|${item.amount}|$decision|$ref\n"); writer.flush()
                pending = null
                runOnUiThread {
                    approve.isEnabled = false; decline.isEnabled = false
                    status.text = if (decision == "APPROVED") "پاسخ موفق آزمایشی ارسال شد؛ پرداخت واقعی نیست." else "پاسخ ناموفق آزمایشی ارسال شد."
                }
            } catch (e: Exception) {
                runOnUiThread { status.text = "ارتباط قطع شد: ${e.message ?: e.javaClass.simpleName}" }
            }
        }
    }

    private fun decide(value: String) {
        val p = pending ?: return
        if (p.decision != null) return
        p.decision = value
        approve.isEnabled = false; decline.isEnabled = false
        status.text = if (value == "APPROVED") "در حال ارسال پاسخ موفق آزمایشی…" else "در حال ارسال پاسخ ناموفق آزمایشی…"
        p.latch.countDown()
    }

    override fun onDestroy() {
        running = false
        try { server?.close() } catch (_: Exception) { }
        pending?.let { if (it.decision == null) { it.decision = "DECLINED"; it.latch.countDown() } }
        super.onDestroy()
    }

    companion object { private val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") }
}
