package com.willismarques.projetowhatsappfirebase.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.willismarques.projetowhatsappfirebase.R
import com.willismarques.projetowhatsappfirebase.activitys.MensagensActivity
import com.willismarques.projetowhatsappfirebase.adapters.ContatosAdapter
import com.willismarques.projetowhatsappfirebase.adapters.ConversasAdapter
import com.willismarques.projetowhatsappfirebase.databinding.FragmentContatosBinding
import com.willismarques.projetowhatsappfirebase.databinding.FragmentConversasBinding
import com.willismarques.projetowhatsappfirebase.model.Conversa
import com.willismarques.projetowhatsappfirebase.model.Usuario
import com.willismarques.projetowhatsappfirebase.utils.Constantes
import com.willismarques.projetowhatsappfirebase.utils.exibirMensagem

class ConversasFragment : Fragment() {

    private lateinit var binding: FragmentConversasBinding

    private lateinit var eventoSnapshot: ListenerRegistration //Colocando como atributo da classe para usar no onDestroi

    private lateinit var conversasAdapter: ConversasAdapter

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversasBinding.inflate(
            inflater, container, false
        )

        conversasAdapter = ConversasAdapter {conversa ->
            val intent = Intent(context, MensagensActivity::class.java)

            val usuario = Usuario(
                id = conversa.idUsuarioDestinatario,
                nome = conversa.nome,
                foto = conversa.foto
            )
            intent.putExtra("dadosDestinatario", usuario)
            startActivity(intent)
        }
        binding.rvConversas.adapter = conversasAdapter
        binding.rvConversas.layoutManager = LinearLayoutManager(context)
        binding.rvConversas.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerConversas()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }

    private fun adicionarListenerConversas() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (idUsuarioRemetente != null){
            eventoSnapshot = firebaseFirestore
                .collection(Constantes.CONVERSAS)
                .document(idUsuarioRemetente)
                .collection(Constantes.ULTIMAS_CONVERSAS)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null){
                    }
                    val listaConversa = mutableListOf<Conversa>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach {documentSnapshot ->
                        val conversa = documentSnapshot.toObject(Conversa::class.java)
                        if (conversa != null){
                            listaConversa.add(conversa)
                        }
                    }

                    if (listaConversa.isNotEmpty()){
                        conversasAdapter.adicionarListaConversas(listaConversa)

                    }

                }
        }
    }

}