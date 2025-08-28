package com.mobitech.inventarioabc.ui.leitura

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mobitech.inventarioabc.R
import com.mobitech.inventarioabc.databinding.FragmentLeituraBinding

class LeituraFragment : Fragment() {

    private var _binding: FragmentLeituraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeituraViewModel by viewModels()

    private val selectFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onFolderSelected(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeituraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        configureCodigoFieldStatic()
        setupImeActions()
    }

    private fun configureCodigoFieldStatic() {
        // Impede teclado virtual automático mas permite foco / uso de teclado físico
        binding.editCodigo.showSoftInputOnFocus = false
        binding.editCodigo.isFocusable = true
        binding.editCodigo.isFocusableInTouchMode = true
        binding.editCodigo.isClickable = true
        binding.editCodigo.isCursorVisible = true
        // Ao ganhar foco não aciona teclado
        binding.editCodigo.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideSoftKeyboard(v)
            }
        }
        // Clique direto só foca (permite hardware keyboard)
        binding.editCodigo.setOnClickListener {
            if (!binding.editCodigo.hasFocus()) binding.editCodigo.requestFocus()
            hideSoftKeyboard(it)
        }
    }

    private fun setupObservers() {
        viewModel.confirmResult.observe(viewLifecycleOwner) { result ->
            if (result.isDuplicate) {
                showDuplicateDialog(binding.editCodigo.text.toString(), result.existingQuantity)
            } else {
                if (result.success) {
                    clearFields()
                    showSnackbar(result.message)
                } else {
                    showSnackbar(result.message)
                }
            }
        }

        viewModel.needsFolderSelection.observe(viewLifecycleOwner) { needs ->
            if (needs) {
                showFolderSelectionDialog()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener { showFolderSelectionDialog() }

        // Ícone teclado: abre soft keyboard sem alterar comportamento padrão posterior
        binding.layoutCodigo.setEndIconOnClickListener { openKeyboardForCodigo() }

        binding.btnConfirmar.setOnClickListener { onConfirmar() }
        binding.btnVerItens.setOnClickListener { findNavController().navigate(R.id.action_leitura_to_lista) }
    }

    private fun openKeyboardForCodigo() {
        // Ativa temporariamente a abertura do teclado apenas neste momento
        binding.editCodigo.showSoftInputOnFocus = true
        binding.editCodigo.requestFocus()
        showKeyboard(binding.editCodigo)
        // Reverte para evitar abertura automática em focos futuros (post para deixar teclado abrir agora)
        binding.editCodigo.post {
            binding.editCodigo.showSoftInputOnFocus = false
        }
    }

    private fun onConfirmar() {
        val codigo = binding.editCodigo.text.toString().trim()
        val quantidadeText = binding.editQuantidade.text.toString().trim()

        if (codigo.isEmpty()) {
            showSnackbar("Digite um código primeiro")
            return
        }
        if (quantidadeText.isEmpty()) {
            binding.editQuantidade.error = "Campo obrigatório"
            binding.editQuantidade.requestFocus()
            showKeyboard(binding.editQuantidade)
            return
        }
        try {
            val quantidade = quantidadeText.toInt()
            if (quantidade <= 0) {
                binding.editQuantidade.error = "Quantidade deve ser maior que 0"
                binding.editQuantidade.requestFocus(); showKeyboard(binding.editQuantidade); return
            }
            hideSoftKeyboard(binding.editQuantidade)
            viewModel.confirmRead(codigo, quantidade)
        } catch (_: NumberFormatException) {
            binding.editQuantidade.error = "Quantidade inválida"
            binding.editQuantidade.requestFocus(); showKeyboard(binding.editQuantidade)
        }
    }

    private fun clearFields() {
        binding.editCodigo.text?.clear()
        binding.editQuantidade.text?.clear()
        // Restaurar foco para código para fluxo rápido, sem teclado virtual
        binding.editCodigo.showSoftInputOnFocus = false
        binding.editCodigo.requestFocus()
        hideSoftKeyboard(binding.editCodigo)
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showDuplicateDialog(codigo: String, currentQuantity: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.codigo_ja_lido_title)
            .setMessage(R.string.codigo_ja_lido_message)
            .setPositiveButton(R.string.alterar) { _, _ -> showEditQuantityDialog(codigo, currentQuantity) }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun showEditQuantityDialog(codigo: String, currentQuantity: Int) {
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
                    val newQ = input.text.toString().toInt()
                    if (newQ > 0) viewModel.updateExistingItem(codigo, newQ) else showSnackbar(getString(R.string.quantidade_invalida))
                } catch (_: NumberFormatException) { showSnackbar(getString(R.string.quantidade_invalida)) }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun showFolderSelectionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.pasta_inventario_title)
            .setMessage(R.string.pasta_inventario_message)
            .setPositiveButton(R.string.confirmar) { _, _ -> selectFolderLauncher.launch(null) }
            .setCancelable(false)
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupImeActions() {
        binding.editCodigo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                binding.editQuantidade.requestFocus()
                true
            } else false
        }
        binding.editQuantidade.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onConfirmar()
                true
            } else false
        }
        // Suporte a teclado físico: ENTER / NUMPAD_ENTER
        binding.editCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                binding.editQuantidade.requestFocus()
                return@setOnKeyListener true
            }
            false
        }
        binding.editQuantidade.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                hideSoftKeyboard(v)
                onConfirmar()
                return@setOnKeyListener true
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
