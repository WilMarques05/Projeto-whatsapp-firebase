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
import com.willismarques.projetowhatsappfirebase.activitys.MensagensActivity
import com.willismarques.projetowhatsappfirebase.adapters.ContatosAdapter
import com.willismarques.projetowhatsappfirebase.databinding.FragmentContatosBinding
import com.willismarques.projetowhatsappfirebase.model.Usuario
import com.willismarques.projetowhatsappfirebase.utils.Constantes

class ContatosFragment : Fragment() {
    private lateinit var binding: FragmentContatosBinding

    private lateinit var eventoSnapshot: ListenerRegistration

    private lateinit var contatosAdapter: ContatosAdapter

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
        binding = FragmentContatosBinding.inflate(
            inflater, container, false
        )

        contatosAdapter = ContatosAdapter {usuarios ->
            val intent = Intent(context, MensagensActivity::class.java)
            intent.putExtra("dadosDestinatario", usuarios)
            intent.putExtra("origem", Constantes.ORIGEM_CONTATO)
            startActivity(intent)
        }
        binding.rvContatos.adapter = contatosAdapter
        binding.rvContatos.layoutManager = LinearLayoutManager(context)
        binding.rvContatos.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerContatos()
    }

    private fun adicionarListenerContatos() {


        eventoSnapshot = firebaseFirestore
            .collection(Constantes.USUARIOS)
            .addSnapshotListener { querySnapshot, erro ->
                val listaContatos = mutableListOf<Usuario>()
                val documentos = querySnapshot?.documents
                documentos?.forEach { documentSnapshot ->
                    val idUsuarioLogado = firebaseAuth.currentUser?.uid
                    val usuarios = documentSnapshot.toObject(Usuario::class.java)
                    if (usuarios != null && idUsuarioLogado != null){
                        if (idUsuarioLogado != usuarios.id ){
                            listaContatos.add(usuarios)
                        }
                    }
                }
                if (listaContatos.isNotEmpty()){
                    contatosAdapter.adicionarLista(listaContatos)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }

}