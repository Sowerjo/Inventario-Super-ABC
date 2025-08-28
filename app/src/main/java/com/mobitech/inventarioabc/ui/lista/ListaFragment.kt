package com.mobitech.inventarioabc.ui.lista

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mobitech.inventarioabc.R
import com.mobitech.inventarioabc.databinding.FragmentListaBinding
import androidx.core.widget.addTextChangedListener

class ListaFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListaViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupSearch()

        // Título inicial
        activity?.title = getString(R.string.lista_title_base)
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(
            onEditClick = { item ->
                showEditDialog(item.codigo, item.quantidade)
            },
            onDeleteClick = { item ->
                showDeleteConfirmation(item.codigo)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ListaFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)

            // Atualizar título com contagem
            activity?.title = getString(R.string.lista_title_count, items.size)

            // Mostrar/ocultar mensagem vazia
            if (items.isEmpty()) {
                binding.textEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.textEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            showSnackbar(result.message)
        }
    }

    private fun setupSearch() {
        val editSearch = binding.root.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editSearch)
        editSearch?.addTextChangedListener { text ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
        editSearch?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else false
        }
    }

    private fun showEditDialog(codigo: String, currentQuantity: Int) {
        val input = TextInputEditText(requireContext()).apply {
            setText(currentQuantity.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            selectAll()
        }

        val inputLayout = TextInputLayout(requireContext()).apply {
            hint = getString(R.string.nova_quantidade_label)
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.editar_quantidade_title)
            .setView(inputLayout)
            .setPositiveButton(R.string.confirmar) { _, _ ->
                try {
                    val newQuantity = input.text.toString().toInt()
                    viewModel.editQuantity(codigo, newQuantity)
                } catch (_: NumberFormatException) {
                    showSnackbar(getString(R.string.quantidade_invalida))
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun showDeleteConfirmation(codigo: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirmar_exclusao_title)
            .setMessage(getString(R.string.confirmar_exclusao_message, codigo))
            .setPositiveButton(R.string.excluir) { _, _ ->
                viewModel.deleteItem(codigo)
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
