package com.example.testmaplibre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.testmaplibre.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var mapLibreMap: MapLibreMap

	private var _selectTypeLayer = MutableStateFlow(TypeLayer.Fill)
	private val state: StateFlow<TypeLayer> = _selectTypeLayer


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		MapLibre.getInstance(this)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.mapView.onCreate(savedInstanceState)
		setupMap()

		binding.btnLine.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Line

			changeLatLong(35.7289219489, 139.7640746734)
			layerMap(mapLibreMap.style!!, TypeLayer.Line, "bunkyo")
		}
		binding.btnFill.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Fill
			changeLatLong(35.70725, 139.72825)
			layerMap(mapLibreMap.style!!, TypeLayer.Fill, "chiba_fld")
		}
		binding.btnPark.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Park
			changeLatLong(35.705499694, 139.74964246)
			layerMap(mapLibreMap.style!!, TypeLayer.Park, "park")
		}
		binding.btnLandmark.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Landmark
			changeLatLong(35.7035806436, 139.7471405047)
			layerMap(mapLibreMap.style!!, TypeLayer.Landmark, "landmark")
		}
		binding.btnRailway.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Railway
			changeLatLong(35.705499694, 139.74964246)
			layerMap(mapLibreMap.style!!, TypeLayer.Railway, "railway")
		}
		binding.btnEmergency.setOnClickListener {
			_selectTypeLayer.value = TypeLayer.Emergency
			changeLatLong(35.730824737, 139.741585443)
			layerMap(mapLibreMap.style!!, TypeLayer.Emergency, "emergency")
		}
	}

	private fun setupMap() {
		binding.mapView.getMapAsync { map ->
			mapLibreMap = map
			// 2d bright // 3d liberty
			map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
				// show 3d building from asset geojson
				add3DLayer(style)

				AppCompatResources.getDrawable(this, R.drawable.ic_park)?.let { drawable ->
					style.addImage("ic_park", drawable)
				}
				AppCompatResources.getDrawable(this, R.drawable.ic_landmark)?.let { drawable ->
					style.addImage("ic_landmark", drawable)
				}

				layerMap(style, state.value, "chiba_fld")
			}
			map.cameraPosition =
				CameraPosition.Builder()
					.target(LatLng(35.70725, 139.72825))
					.zoom(15.0).build()
		}
	}

	private fun changeLatLong(
		latitude: Double,
		longitude: Double,
	) {
		mapLibreMap.cameraPosition =
			CameraPosition.Builder()
				.target(LatLng(latitude, longitude))
				.zoom(15.0).build()
	}

	private fun createGeoJsonSource(style: Style, id:String, geoJsonString:  String): String{
		val geoJsonSource = GeoJsonSource("$id-source", geoJsonString)
		style.addSource(geoJsonSource)
		return "$id-source"
	}

	private fun layerMap(style: Style, type: TypeLayer, geoJsonName: String) {
		Log.d("TAG", "layerMap type: ${type.name.lowercase()}")
		try {
			// Read GeoJSON from assets
			val layers = style.layers.toList()

			Log.d("TAG", "layerMap layers: ${layers.size}")

			for (layer in layers) {

				Log.d(
					"TAG", "layerMap layers: ${
						layer.let {
							it.id
						}
					}"
				)
			}

			for (typeLayer in TypeLayer.values()){
				style.removeLayer("${typeLayer.name.lowercase()}-layer")
				style.removeSource("${typeLayer.name.lowercase()}-source")
			}

			val geoJsonString = loadGeoJsonFromAsset("$geoJsonName.geojson")

			// Create GeoJSON source
			val sourceId = createGeoJsonSource(style, type.name.lowercase(), geoJsonString)

			// Create a line layer to render the LineString
			when (type) {
				TypeLayer.Fill -> {
					val fillLayer = FillLayer("${type.name.lowercase()}-layer", sourceId)

					// Style the line with more explicit properties
					fillLayer.setProperties(
						// Ensure line is visible
						PropertyFactory.fillColor(Color.RED),
						PropertyFactory.fillOpacity(0.7f),
					)

					// Add the layer to the map

					style.addLayerAbove(fillLayer, "background")
				}

				TypeLayer.Line -> {
					val lineLayer = LineLayer("${type.name.lowercase()}-layer", sourceId)

					// Style the line with more explicit properties
					lineLayer.setProperties(
						// Ensure line is visible
						PropertyFactory.lineColor(android.graphics.Color.RED),
						PropertyFactory.lineWidth(5f),
						PropertyFactory.lineOpacity(1f),
						PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
						PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
					)

					// Add the layer to the map
					style.addLayer(lineLayer)
				}
				TypeLayer.Railway -> {
					val lineLayer = LineLayer("${type.name.lowercase()}-layer", sourceId)

					// Style the line with more explicit properties
					lineLayer.setProperties(
						// Ensure line is visible
						PropertyFactory.lineColor(android.graphics.Color.RED),
						PropertyFactory.lineWidth(5f),
						PropertyFactory.lineOpacity(1f),
						PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
						PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
					)

					// Add the layer to the map
					style.addLayer(lineLayer)
				}

				TypeLayer.Park -> {
					val markerLayer = SymbolLayer("${type.name.lowercase()}-layer", sourceId)
						.withProperties(
							PropertyFactory.iconImage("ic_park"),
							PropertyFactory.iconAllowOverlap(true),
							PropertyFactory.iconSize(1.0f),
							// Optional: Add text labels
							PropertyFactory.textField(Expression.get("公園名")),
							PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
							PropertyFactory.textAnchor("top")
						)

					style.addLayer(markerLayer)
				}
				TypeLayer.Landmark -> {
					val markerLayer = SymbolLayer("${type.name.lowercase()}-layer", sourceId)
						.withProperties(
							PropertyFactory.iconImage("ic_landmark"),
							PropertyFactory.iconAllowOverlap(true),
							PropertyFactory.iconSize(1.0f),
							// Optional: Add text labels
							PropertyFactory.textField(Expression.get("名称")),
							PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
							PropertyFactory.textAnchor("top")
						)

					style.addLayer(markerLayer)
				}

				TypeLayer.Emergency -> {
					val lineLayer = LineLayer("${type.name.lowercase()}-layer", sourceId)

					// Style the line with more explicit properties
					lineLayer.setProperties(
						// Ensure line is visible
						PropertyFactory.lineColor(android.graphics.Color.RED),
						PropertyFactory.lineWidth(5f),
						PropertyFactory.lineOpacity(1f),
						PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
						PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
					)

					// Add the layer to the map
					style.addLayer(lineLayer)
				}
			}


		} catch (e: Exception) {
			Log.e("GeoJSON", "Error loading GeoJSON", e)
		}
	}

	private fun add3DLayer(style: Style) {
		try {
			// Load GeoJSON from the assets folder
			val geoJson = loadGeoJsonFromAsset("sample.geojson")

			// Add GeoJsonSource
			val geoJsonSource = GeoJsonSource("geojson-source", geoJson)
			style.addSource(geoJsonSource)

			val altitudes = parseAltitudeFromGeoJson(geoJson)
			// Add FillExtrusionLayer
			val lineExtrusionLayer =
				FillExtrusionLayer("line-extrusion-layer", "geojson-source").apply {
					setProperties(
						PropertyFactory.fillExtrusionColor(Color.BLUE), // Line color (extrusion color)
						PropertyFactory.fillExtrusionHeight(
							altitudes.average().toFloat()
						), // Set height (altitude)
						PropertyFactory.fillExtrusionBase(0.0f), // Base height (floor level)
						PropertyFactory.fillExtrusionOpacity(0.7f),
					)
				}

			// Add layer to the map
			style.addLayer(lineExtrusionLayer)

		} catch (e: Exception) {
			Log.e("3D Layer", "Error adding 3D layer", e)
		}
	}

	private fun parseAltitudeFromGeoJson(geoJsonString: String): List<Float> {
		val altitudes = mutableListOf<Float>()

		// Parse the GeoJSON
		val jsonObject = JSONObject(geoJsonString)
		val features = jsonObject.getJSONArray("features")

		for (i in 0 until features.length()) {
			val feature = features.getJSONObject(i)
			val geometry = feature.getJSONObject("geometry")

			// Check if coordinates exist and extract altitude
			if (geometry.has("coordinates")) {
				val coordinates = geometry.getJSONArray("coordinates")
				// Extract altitude (third element) from each coordinate
				for (j in 0 until coordinates.length()) {
					val coordinate = coordinates.getJSONArray(j)
					val altitude = coordinate.optDouble(2, 0.0) // Default to 0.0 if no altitude
					altitudes.add(altitude.toFloat())
				}
			}
		}

		return altitudes
	}

	private fun loadGeoJsonFromAsset(fileName: String): String {
		return try {
			val inputStream = assets.open(fileName)
			val size = inputStream.available()
			val buffer = ByteArray(size)
			inputStream.read(buffer)
			inputStream.close()
			String(buffer, Charset.forName("UTF-8"))
		} catch (e: IOException) {
			e.printStackTrace()
			""
		}
	}

	override fun onStart() {
		super.onStart()
		binding.mapView.onStart()
	}

	override fun onResume() {
		super.onResume()
		binding.mapView.onResume()
	}

	override fun onPause() {
		super.onPause()
		binding.mapView.onPause()
	}

	override fun onStop() {
		super.onStop()
		binding.mapView.onStop()
	}

	override fun onLowMemory() {
		super.onLowMemory()
		binding.mapView.onLowMemory()
	}

	override fun onDestroy() {
		super.onDestroy()
		binding.mapView.onDestroy()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		binding.mapView.onSaveInstanceState(outState)
	}
}

enum class TypeLayer {
	Line, Fill, Park, Railway, Landmark, Emergency
}