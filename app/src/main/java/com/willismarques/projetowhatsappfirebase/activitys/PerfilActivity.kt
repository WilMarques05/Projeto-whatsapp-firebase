package com.willismarques.projetowhatsappfirebase.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.willismarques.projetowhatsappfirebase.databinding.ActivityPerfilBinding
import com.willismarques.projetowhatsappfirebase.utils.exibirMensagem

class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    private val firebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false


    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri ->
        if (uri != null){
            binding.imagePerfil.setImageURI(uri)
            uploadImagemStorage(uri)
        }else{
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
        recuperarDadosIniciaisUsuario()
    }

    override fun onStart() {
        super.onStart()
        recuperarDadosIniciaisUsuario()
    }

    private fun recuperarDadosIniciaisUsuario() {
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario != null){
            firebaseFirestore
                .collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val dadosUsuario = documentSnapshot.data
                    if (dadosUsuario != null){
                        val nome = dadosUsuario["nome"] as String
                        val foto = dadosUsuario["foto"] as String

                        binding.editNomePerfil.setText(nome)
                        if (foto.isNotEmpty()){
                            Picasso.get()
                                .load(foto)
                                .into(binding.imagePerfil)
                        }
                    }
                }
        }
    }

    private fun uploadImagemStorage(uri: Uri) {
        /*
        Estrutura de pastas no storage
        fotos -> usuarios -> idUsuario -> perfil.jpg
        */
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario != null){
            firebaseStorage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener {task ->
                    exibirMensagem("Sucesso ao fazer upload da imagem")
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener {url ->
                            //Atualizando dados do usuario no fireStore que é o banco de dados
                            val dados = mapOf(
                                "foto" to url.toString()
                            )
                            atualizarDadosPerfil(idUsuario, dados)
                        }
                }.addOnFailureListener {
                    exibirMensagem("Erro ao fazer upload da imagem")
                }
        }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {
        firebaseFirestore
            .collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao atualizar perfil")
            }.addOnFailureListener {
                exibirMensagem("Erro ao atualizar perfil")
            }
    }

    private fun inicializarEventosClique() {
        binding.fabSelecionar.setOnClickListener {
            if (temPermissaoGaleria){
                gerenciadorGaleria.launch("image/*")
            }else{
                exibirMensagem("Dê a permissão para acessar a galeria")
                solicitarPermissoes()
            }
        }
        binding.btnAtualizarPerfil.setOnClickListener {
            val nomeUsuario = binding.editNomePerfil.text.toString()
            if (nomeUsuario.isNotEmpty()){
                val idUsuario = firebaseAuth.currentUser?.uid
                if (idUsuario != null){
                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)
                }
            }else{
                exibirMensagem("Preencha o nome para atualizar")
            }
        }
    }

    private fun solicitarPermissoes() {

        //Verificar se o usuario tem permissão
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        //Lista de permissões negadas
        val listaPermissoesNegadas = mutableListOf<String>()
        if ( !temPermissaoCamera){
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        }
        if (!temPermissaoGaleria){
            listaPermissoesNegadas.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (listaPermissoesNegadas.isNotEmpty()){
            //solicitar Mutiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){permissoes ->
                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria
            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includtoolbarPerfil.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar Perfil"
            setDisplayHomeAsUpEnabled(true)
        }

    }
}