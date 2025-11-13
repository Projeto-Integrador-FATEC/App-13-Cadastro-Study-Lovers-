package com.example.app13cadastrostudylovers

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import java.lang.RuntimeException

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import java.net.URL

import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection


class MainActivity : AppCompatActivity() {
    val API_STUDYLOVERS = "http://10.0.2.2:4080"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mostrarTelaCadastro()
    }

    fun fazerCadastro(nome: String, email: String, senha: String) {
        // Executa em thread separada
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$API_STUDYLOVERS/api/users") // ex: endpoint /cadastro
                val conexao = url.openConnection() as HttpURLConnection
                conexao.requestMethod = "POST"
                conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conexao.doOutput = true

                // Corpo da requisição (JSON)
                val json = JSONObject()
                json.put("name", nome)
                json.put("email", email)
                json.put("password", senha)

                val outputStream = DataOutputStream(conexao.outputStream)
                outputStream.writeBytes(json.toString())
                outputStream.flush()
                outputStream.close()

                // Lê a resposta da API
                val responseCode = conexao.responseCode
                val resposta = conexao.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    Log.d("API", "Código: $responseCode - Resposta: $resposta")
                    // aqui você pode atualizar a UI com a resposta
                }

                conexao.disconnect()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API", "Erro: ${e.message}")
                }
            }
        }
    }

    fun fazerLogin(email: String, senha: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Se estiver no emulador e a API rodando no PC:
                val url = URL("http://10.0.2.2:4080/api/users/login")
                val conexao = url.openConnection() as HttpURLConnection
                conexao.requestMethod = "POST"
                conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conexao.doOutput = true

                // Cria o JSON com as credenciais
                val json = JSONObject()
                json.put("email", email)
                json.put("password", senha)

                // Envia o corpo da requisição
                val outputStream = DataOutputStream(conexao.outputStream)
                outputStream.writeBytes(json.toString())
                outputStream.flush()
                outputStream.close()

                // Lê o código e resposta
                val responseCode = conexao.responseCode
                val resposta = if (responseCode == 200) {
                    conexao.inputStream.bufferedReader().use { it.readText() }
                } else {
                    conexao.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        Log.d("LOGIN", "Login bem-sucedido: $resposta")
                        // 👉 aqui você pode salvar dados do usuário e trocar de tela
                        mostrarTelaConfirmada()
                    } else if (responseCode == 401) {
                        Log.w("LOGIN", "Credenciais inválidas")
                        // 👉 mostrar mensagem de erro na tela
                    } else {
                        Log.e("LOGIN", "Erro ${responseCode}: $resposta")
                    }
                }

                conexao.disconnect()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LOGIN", "Erro: ${e.message}")
                }
            }
        }
    }

    private fun mostrarTelaCadastro() {
        setContentView(R.layout.cadastro)
        val txt_nomeCadatro = findViewById<TextView>(R.id.txt_nomeCadastro)
        val txt_emailCadastro = findViewById<TextView>(R.id.txt_emailCadastro)
        val txt_senhaCadastro = findViewById<TextView>(R.id.txt_senhaCadastro)
        val txtIrParaLogin = findViewById<TextView>(R.id.ir_para_login)
        val btnCadastrar = findViewById<Button>(R.id.buttonCadastrar)

        txtIrParaLogin.setOnClickListener {
            mostrarTelaLogin()
        }

        btnCadastrar.setOnClickListener {
            val nome = txt_nomeCadatro.text.toString()
            val email = txt_emailCadastro.text.toString()
            val senha = txt_senhaCadastro.text.toString()
            fazerCadastro(nome, email, senha)
            mostrarTelaLogin()
        }
    }

    private fun mostrarTelaLogin() {
        setContentView(R.layout.login)

        val txtVoltarParaCadastro: TextView = findViewById(R.id.voltar_para_cadastro)
        val btnEntrar: Button = findViewById(R.id.buttonEntrar)
        val txt_emailLogin: EditText = findViewById(R.id.txt_emailLogin)
        val txt_senhaLogin: EditText = findViewById(R.id.txt_senhaLogin)


        txtVoltarParaCadastro.setOnClickListener {
            mostrarTelaCadastro()
        }

        btnEntrar.setOnClickListener {
            val email = txt_emailLogin.text.toString()
            val senha = txt_senhaLogin.text.toString()

            fazerLogin(email, senha)
        }
    }

    private fun mostrarTelaConfirmada() {
        setContentView(R.layout.login_confirmado)
    }
}

