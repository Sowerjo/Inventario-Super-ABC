package com.mobitech.inventarioabc.ui.lista

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobitech.inventarioabc.R
import com.mobitech.inventarioabc.databinding.ItemInventoryBinding
import com.mobitech.inventarioabc.domain.model.InventoryItem
import com.mobitech.inventarioabc.util.DateTimeFmt

class InventoryAdapter(
    private val onEditClick: (InventoryItem) -> Unit,
    private val onDeleteClick: (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem) {
            binding.textCodigo.text = item.codigo

            val dataFormatada = DateTimeFmt.formatToDisplay(item.dataHoraEpochMillis)
            binding.textDetalhes.text = binding.root.context.getString(
                R.string.qtd_data_format,
                item.quantidade,
                dataFormatada
            )

            binding.btnEditar.setOnClickListener {
                onEditClick(item)
            }

            binding.btnExcluir.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem.codigo == newItem.codigo
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
