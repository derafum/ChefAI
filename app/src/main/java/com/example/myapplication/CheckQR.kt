import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class CheckQR : AsyncTask<String, Void, List<String>>() {

    fun trimQRCode(qrCode: String): String {
        val startIndex = qrCode.indexOf("t=")
        if (startIndex != -1) {
            return qrCode.substring(startIndex)
        }
        return qrCode
    }

    override fun doInBackground(vararg params: String): List<String> {
        val trimmedQRCode = trimQRCode(params[0])

        val token = "20253.Umk1V1xWs9M87DoWY"
        val url = URL("https://proverkacheka.com/api/v1/check/get")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val data = JSONObject()
        data.put("token", token)
        data.put("qrraw", trimmedQRCode)

        val outputStream: OutputStream = connection.outputStream
        outputStream.write(data.toString().toByteArray())
        outputStream.flush()

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = StringBuilder()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                response.append(line)
                line = reader.readLine()
            }
            reader.close()

            val jsonData = JSONObject(response.toString())
            val names = jsonData.getJSONObject("data").getJSONObject("json").getJSONArray("items")
            val list2 = listOf(
                "томат", "огурц", "помидор", "молоко", "персик", "апельсин", "морковь", "мандарин",
                "перец", "баклажан", "картофель", "картошк", "перец", "яйц", "яблок", "чеснок",
                "лимон", "репчатый лук", "банан", "груш", "цукин", "капуст", "клубника", "куриц",
                "курин", "свинин", "вишн", "виноград", "лук", "гриб", "говядин"
            )

            val newList = mutableListOf<String>()
            for (i in 0 until names.length()) {
                val item = names.getJSONObject(i).getString("name").toLowerCase()
                for (product in list2) {
                    if (item.contains(product)) {
                        newList.add(product)
                        break
                    }
                }
            }

            return newList
        } else {
            Log.e("CheckReceiptTask", "HTTP Error: $responseCode")
            return emptyList()
        }
    }

    private fun logResults(result: List<String>) {
        for (product in result) {
            Log.d("CheckQR", "Product: $product")
        }
    }

    override fun onPostExecute(result: List<String>) {
        logResults(result)
    }
}
