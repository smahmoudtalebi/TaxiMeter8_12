package ir.taximeter.core.payment

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import kotlin.coroutines.resume

/**
 * TEST-ONLY gateway. Talks to the companion TaxiMeter Receiver test app over Bluetooth Classic RFCOMM.
 * Its APPROVED response is a human-triggered simulation, NOT a bank/POS approval.
 */
class BluetoothTestPosPaymentGateway(private val activity: Activity) : PosPaymentGateway {
    override suspend fun charge(request: PosPaymentRequest): PosPaymentResult {
        val device = choosePairedDevice() ?: return PosPaymentResult.Failed("انتخاب دستگاه لغو شد یا دستگاه جفت‌شده‌ای وجود ندارد.")
        return withContext(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: return@withContext PosPaymentResult.Failed("این گوشی بلوتوث ندارد.")
                if (!adapter.isEnabled) return@withContext PosPaymentResult.Failed("بلوتوث گوشی خاموش است.")
                adapter.cancelDiscovery()
                socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                socket.connect()
                val writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
                val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
                val trip = request.tripId.orEmpty().replace('|', '_').replace('\n', '_')
                writer.write("PAY|${request.requestId}|${request.amountToman}|$trip\n")
                writer.flush()
                val response = reader.readLine() ?: return@withContext PosPaymentResult.Failed("گیرنده پاسخ نداد.")
                val fields = response.split('|')
                if (fields.size < 5 || fields[0] != "RESULT")
                    return@withContext PosPaymentResult.Failed("قالب پاسخ گیرنده نامعتبر است.")
                if (fields[1] != request.requestId || fields[2] != request.amountToman.toString())
                    return@withContext PosPaymentResult.Failed("شناسه یا مبلغ پاسخ با درخواست تطبیق ندارد؛ پرداخت تأیید نشد.")
                when (fields[3]) {
                    "APPROVED" -> PosPaymentResult.Approved("TEST:${fields.drop(4).joinToString("|")}")
                    "DECLINED" -> PosPaymentResult.Declined(fields.drop(4).joinToString("|"))
                    else -> PosPaymentResult.Failed("وضعیت پاسخ ناشناخته است.")
                }
            } catch (e: Exception) {
                PosPaymentResult.Failed("ارتباط آزمایشی بلوتوث ناموفق بود: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                try { socket?.close() } catch (_: Exception) { }
            }
        }
    }

    private suspend fun choosePairedDevice(): BluetoothDevice? = suspendCancellableCoroutine { cont ->
        activity.runOnUiThread {
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter == null) {
                    Toast.makeText(activity, "این گوشی بلوتوث ندارد.", Toast.LENGTH_LONG).show()
                    cont.resume(null); return@runOnUiThread
                }
                if (!adapter.isEnabled) {
                    Toast.makeText(activity, "ابتدا بلوتوث را روشن کنید.", Toast.LENGTH_LONG).show()
                    cont.resume(null); return@runOnUiThread
                }
                val devices = adapter.bondedDevices?.toList()?.sortedBy { it.name ?: it.address } ?: emptyList()
                if (devices.isEmpty()) {
                    Toast.makeText(activity, "ابتدا دو گوشی را از تنظیمات بلوتوث Pair کنید.", Toast.LENGTH_LONG).show()
                    cont.resume(null); return@runOnUiThread
                }
                val labels = devices.map { "${it.name ?: "بدون نام"} (${it.address})" }.toTypedArray()
                AlertDialog.Builder(activity)
                    .setTitle("انتخاب گوشی گیرندهٔ آزمایشی")
                    .setAdapter(ArrayAdapter(activity, android.R.layout.simple_list_item_1, labels)) { _, which ->
                        if (cont.isActive) cont.resume(devices[which])
                    }
                    .setNegativeButton("انصراف") { _, _ -> if (cont.isActive) cont.resume(null) }
                    .setOnCancelListener { if (cont.isActive) cont.resume(null) }
                    .show()
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    companion object {
        // Standard Serial Port Profile UUID, used by both test apps.
        val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
