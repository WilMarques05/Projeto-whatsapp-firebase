package com.willismarques.projetowhatsappfirebase.activitys

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.willismarques.projetowhatsappfirebase.adapters.MensagensAdapter
import com.willismarques.projetowhatsappfirebase.databinding.ActivityMensagensBinding
import com.willismarques.projetowhatsappfirebase.model.Conversa
import com.willismarques.projetowhatsappfirebase.model.Mensagem
import com.willismarques.projetowhatsappfirebase.model.Usuario
import com.willismarques.projetowhatsappfirebase.utils.Constantes
import com.willismarques.projetowhatsappfirebase.utils.exibirMensagem

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private var dadosDestinatario: Usuario? = null
    private var dadosRemetente: Usuario? = null
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var conversasAdapter: MensagensAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventosClique()
        inicializarRecyclerview()
        inicializarListeners()
    }

    private fun inicializarRecyclerview() {
        with(binding){
            conversasAdapter = MensagensAdapter()
            rvMensagens.adapter = conversasAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsiarioDestinatario = dadosDestinatario?.id
        if (idUsuarioRemetente != null && idUsiarioDestinatario != null){
            listenerRegistration = firebaseFirestore
                .collection(Constantes.BD_MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsiarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener{querySnapshot, erro ->
                    if (erro != null){
                        exibirMensagem("Erro ao recuperar mensagem")
                    }
                    val listaMensagens = mutableListOf<Mensagem>()
                    val documentos = querySnapshot?.documents
                    documentos?.forEach { documentSnapshot ->
                        val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                        if (mensagem != null){
                            listaMensagens.add(mensagem)
                        }
                    }
                    if (listaMensagens.isNotEmpty()){
                        //Passando a lista para o adapter
                        conversasAdapter.adicionarLista((listaMensagens))
                    }
                }
        }
    }

    private fun inicializarEventosClique() {
        binding.fabEnviar.setOnClickListener {
            val mensagem = binding.editeMensagem.text.toString()
            salvarMensagem(mensagem)
        }
    }

    private fun salvarMensagem(textoMensagem: String) {
        if (textoMensagem.isNotEmpty()){
            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            if (idUsuarioRemetente != null && idUsuarioDestinatario != null){
                val mensagem = Mensagem(
                    idUsuarioRemetente, textoMensagem
                )
                //Salvar para o remetente
                salvarMensagemFirestore(
                    idUsuarioRemetente, idUsuarioDestinatario, mensagem
                )

                //salvar Foto e nome Destinatario
                val conversaRemetente = Conversa(
                    idUsuarioRemetente, idUsuarioDestinatario,
                    dadosDestinatario!!.foto, dadosDestinatario!!.nome,
                    textoMensagem
                )
                salvarConversaFirestore(conversaRemetente)

                //Salvar para o destinatario
                salvarMensagemFirestore(
                    idUsuarioDestinatario, idUsuarioRemetente, mensagem
                )

                //salvar Foto e nome Remetente
                val conversaDestinatario = Conversa(
                    idUsuarioDestinatario, idUsuarioRemetente,
                    dadosRemetente!!.foto, dadosRemetente!!.nome,
                    textoMensagem
                )
                salvarConversaFirestore(conversaDestinatario)


                binding.editeMensagem.setText("")
            }
        }
    }

    private fun salvarConversaFirestore(conversa: Conversa) {
        firebaseFirestore
            .collection(Constantes.CONVERSAS)
            .document(conversa.idUsuarioRemetente)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(conversa.idUsuarioDestinatario)
            .set(conversa)
            .addOnFailureListener {
                exibirMensagem("Erro ao salvar conversa")
            }
    }

    private fun salvarMensagemFirestore(
        idUsuarioRemetente: String, idUsuarioDestinatario: String, mensagem: Mensagem
    ) {
        firebaseFirestore
            .collection(Constantes.BD_MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .add(mensagem)
            .addOnFailureListener {
                exibirMensagem("Erro ao enviar mensagem")
            }
    }

    private fun inicializarToolbar() {
        val toobar = binding.tbMensagens
        setSupportActionBar(toobar)
        supportActionBar?.apply {
            title = ""
            if (dadosDestinatario != null){
                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recuperarDadosUsuarios() {

        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (idUsuarioRemetente != null){
            firebaseFirestore
                .collection(Constantes.USUARIOS)
                .document(idUsuarioRemetente)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if (usuario != null){
                        dadosRemetente = usuario
                    }
                }
        }

        //Recuperando os dados dos contatos e passando para tela de conversas
        val extras = intent.extras
        if (extras != null){
            dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable("dadosDestinatario", Usuario::class.java)
            }else{
                extras.getParcelable("dadosDestinatario")
            }
        }
    }
}