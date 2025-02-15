package com.example.lugares_v.ui.lugar

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.lugares_v.R
import com.example.lugares_v.databinding.FragmentAddLugarBinding
import com.example.lugares_v.databinding.FragmentUpdateLugarBinding
import com.example.lugares_v.model.Lugar
import com.example.lugares_v.viewmodel.LugarViewModel

class UpdateLugarFragment : Fragment() {

    //se recupera un argumento pasado
    private val args by navArgs<UpdateLugarFragmentArgs>()

    private lateinit var mediaPlayer: MediaPlayer

    private var _binding: FragmentUpdateLugarBinding? = null
    private val binding get() = _binding!!
    private lateinit var lugarViewModel: LugarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lugarViewModel = ViewModelProvider(this).get(LugarViewModel::class.java)

        _binding = FragmentUpdateLugarBinding.inflate(inflater, container, false)

        //se pasan los valores a los campos de la pantalla
        binding.etNombre.setText(args.lugar.nombre)
        binding.etCorreoLugar.setText(args.lugar.correo)
        binding.etTelefono.setText(args.lugar.telefono)
        binding.etWeb.setText(args.lugar.web)

        binding.tvLatitud.text = args.lugar.latitud.toString()
        binding.tvLongitud.text = args.lugar.longitud.toString()
        binding.tvAltura.text = args.lugar.altura.toString()

        binding.btUpdateLugar.setOnClickListener { updateLugar() }
        binding.btDeleteLugar.setOnClickListener { deleteLugar() }

        binding.btEmail.setOnClickListener { escribirCorreo() }
        binding.btPhone.setOnClickListener { llamarLugar() }
        binding.btWhatsapp.setOnClickListener { enviarWhatsApp() }
        binding.btWeb.setOnClickListener { verWeb() }
        binding.btLocation.setOnClickListener { verMapa() }

        if (args.lugar.rutaAudio?.isNotEmpty() == true) {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(args.lugar.rutaAudio)
            mediaPlayer.prepare()
            binding.btPlay.isEnabled=true
        } else {
            binding.btPlay.isEnabled=false
        }
        binding.btPlay.setOnClickListener { mediaPlayer.start() }

        if (args.lugar.rutaimagen?.isNotEmpty() == true) {
            Glide.with(requireContext())
                .load(args.lugar.rutaimagen)
                .fitCenter()
                .into(binding.imagen)
        }

        return binding.root
    }

    private fun escribirCorreo() {
        val valor = binding.etCorreoLugar.text.toString()
        if (valor.isNotEmpty()) { //Si tiene algo se puede intentar enviar el corrreo
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(valor))
            intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.msg_saludos)+" "+binding.etNombre.text)
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_mensaje_correo))
            startActivity(intent)
        } else { //Si está vacío entonces mostrar un error
            Toast.makeText(requireContext(), getString(R.string.msg_data),Toast.LENGTH_LONG).show()
        }
    }

    private fun llamarLugar() {
        val valor = binding.etTelefono.text.toString()
        if (valor.isNotEmpty()) { //Si tiene algo se puede intentar llamar
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$valor")
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
                //Si estamos acá hay que pedir pautorización para hace la llamada
                requireActivity().requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 105)
            } else {
                //Si tenemos los permisos!!!
                requireActivity().startActivity(intent)
            }
        } else { //Si está vacío entonces mostrar un error
            Toast.makeText(requireContext(), getString(R.string.msg_data),Toast.LENGTH_LONG).show()
        }
    }

    private fun enviarWhatsApp() {
        val valor = binding.etTelefono.text.toString()
        if (valor.isNotEmpty()) { //Si tiene algo se puede intentar enviar el mensaje
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = "whatsapp://send?phone=506$valor&text"+getString(R.string.msg_saludos)
            intent.setPackage("com.whatsapp")
            intent.data = Uri.parse(uri)

            startActivity(intent)

        } else { //Si está vacío entonces mostrar un error
            Toast.makeText(requireContext(), getString(R.string.msg_data),Toast.LENGTH_LONG).show()
        }
    }

    private fun verWeb() {
        val valor = binding.etWeb.text.toString()
        if (valor.isNotEmpty()) { //Si tiene algo se puede intentar ver el sitio web
            val uri = "http://$valor"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)

        } else { //Si está vacío entonces mostrar un error
            Toast.makeText(requireContext(), getString(R.string.msg_data),Toast.LENGTH_LONG).show()
        }
    }

    private fun verMapa() {
        val latitud = binding.tvLatitud.text.toString().toDouble()
        val longitud = binding.tvLongitud.text.toString().toDouble()

        if (latitud.isFinite() && longitud.isFinite()) { //Si tiene coordenadas válidas se muestra el mapa
            val uri = "geo:$latitud,$longitud?z18"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        } else { //Si está vacío entonces mostrar un error
            Toast.makeText(requireContext(), getString(R.string.msg_data),Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteLugar() {
        val alerta = AlertDialog.Builder(requireContext())
        alerta.setTitle(R.string.bt_delete_lugar)
        alerta.setMessage(getString(R.string.msg_pregunta_eliminar)+" ${args.lugar.nombre}")
        alerta.setPositiveButton(getString(R.string.msg_si)) {_,_ ->
            lugarViewModel.deleteLugar(args.lugar)
            Toast.makeText(requireContext(), getString(R.string.msg_lugar_deleted),Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        }
        alerta.setNegativeButton(getString(R.string.msg_no)) {_,_ ->}

        alerta.create().show()
    }

    //Efectivamente hace el registro del lugar en la base de datos
    private fun updateLugar() {
        val nombre = binding.etNombre.text.toString()
        val correo = binding.etCorreoLugar.text.toString()
        val telefono = binding.etTelefono.text.toString()
        val web = binding.etWeb.text.toString()
        if (nombre.isNotEmpty()) { //Al menos tenemos un nombre
            val lugar = Lugar(args.lugar.id, nombre, correo, web, telefono,
                args.lugar.latitud,
                args.lugar.longitud,
                args.lugar.altura,
                args.lugar.rutaAudio,
                args.lugar.rutaimagen)
            lugarViewModel.saveLugar(lugar)
            Toast.makeText(requireContext(),getString(R.string.msg_lugar_updated),
            Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        } else { //No hay información del lugar...
            Toast.makeText(requireContext(),getString(R.string.msg_data),
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}