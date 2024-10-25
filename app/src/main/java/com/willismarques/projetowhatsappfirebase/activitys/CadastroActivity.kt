package com.willismarques.projetowhatsappfirebase.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.willismarques.projetowhatsappfirebase.databinding.ActivityCadastroBinding
import com.willismarques.projetowhatsappfirebase.model.Usuario
import com.willismarques.projetowhatsappfirebase.utils.exibirMensagem

class CadastroActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClique()

    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()){
                cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, senha
        ).addOnCompleteListener { resultado ->
            if (resultado.isSuccessful){
                //Salvar dados do usuario no fireStore
                /*
                id, nome, email, foto
                * */
                val idUsuario = resultado.result.user?.uid
                if(idUsuario != null){
                    val usuario = Usuario(
                        idUsuario, nome, email
                    )
                    salvarUsuarioFirestore(usuario)
                }
            }
        }.addOnFailureListener {erro ->
            try {
                throw erro
            }catch (erroSenhaFraca: FirebaseAuthWeakPasswordException){
                erroSenhaFraca.printStackTrace()
                exibirMensagem("Senha fraca, digite uma senha com números, letras e caracteres especiais")
            }catch (erroUsuarioExistente: FirebaseAuthUserCollisionException){
                erroUsuarioExistente.printStackTrace()
                exibirMensagem("E-mail já cadastrado, tente outro E-mail")
            }catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException){
                erroCredenciaisInvalidas.printStackTrace()
                exibirMensagem("E-mail invalido, digite outro e-mail")
            }
        }
    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        firestore
            .collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                exibirMensagem("Cadastro realizado com sucesso")
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
            }.addOnFailureListener {
                exibirMensagem("Erro ao realizar cadastro")
            }
    }

    private fun validarCampos(): Boolean {
        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if (nome.isNotEmpty()) {
            binding.textInputNome.error = null
            if (email.isNotEmpty()) {
                binding.textInputEmail.error = null
                if (senha.isNotEmpty()) {
                    binding.textInputSenha.error = null
                    return true
                } else {
                    binding.textInputSenha.error = "Preencha sua Senha!"
                    return false
                }
            } else {
                binding.textInputEmail.error = "Preencha seu E-mail!"
                return false
            }
        } else {
            binding.textInputNome.error = "Preencha o seu Nome!"
            return false
        }
    }

    private fun inicializarToolbar() {
        //Criando a toolbar/Inicializando Toolbar
        val toolbar = binding.includToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"
            setDisplayHomeAsUpEnabled(true)//Botão de voltar
        }
    }
}