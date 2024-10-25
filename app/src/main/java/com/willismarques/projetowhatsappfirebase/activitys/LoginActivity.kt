package com.willismarques.projetowhatsappfirebase.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.willismarques.projetowhatsappfirebase.databinding.ActivityLoginBinding
import com.willismarques.projetowhatsappfirebase.utils.exibirMensagem

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private lateinit var email: String
    private lateinit var senha: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarEventosClique()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null){
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }

    private fun inicializarEventosClique() {
        binding.textCadastro.setOnClickListener {
            startActivity(
                Intent(this, CadastroActivity::class.java)
            )
        }
        binding.btnLogar.setOnClickListener {
            if (validarCampos()){
                logarUsuario()
            }
        }
    }

    private fun logarUsuario() {
        firebaseAuth.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener {
            exibirMensagem("Bem vindo!")
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }.addOnFailureListener {erro ->
            try {
                throw erro
            }catch(erroUsuarioInvalido: FirebaseAuthInvalidUserException){
                erroUsuarioInvalido.printStackTrace()
                exibirMensagem("E-mail não cadastrado")
            }catch(erroCredenciaisInvalida: FirebaseAuthInvalidCredentialsException){
                erroCredenciaisInvalida.printStackTrace()
                exibirMensagem("E-mail ou senha incorretos!")
            }
        }
    }

    private fun validarCampos(): Boolean {
        email = binding.editTextEmailLogin.text.toString()
        senha = binding.editTextSenhaLogin.text.toString()

        if (email.isNotEmpty()){
            binding.textInputEmailLogin.error = null
            if (senha.isNotEmpty()){
                binding.textInputLayoutSenhaLogin.error = null
                return true
            }else{
                binding.textInputLayoutSenhaLogin.error = "Senha inválida"
                return false
            }
        }else{
            binding.textInputEmailLogin.error = "Preencha um e-mail válido"
            return false
        }
    }
}