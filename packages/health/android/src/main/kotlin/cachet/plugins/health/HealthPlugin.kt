package cachet.plugins.health

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.google.android.gms.fitness.FitnessActivities
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.concurrent.thread
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.SessionInsertRequest

const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1111

class HealthPlugin(val activity: Activity, val channel: MethodChannel) : MethodCallHandler, ActivityResultListener, Result {

    private var result: Result? = null
    private var handler: Handler? = null

    private var BODY_FAT_PERCENTAGE = "BODY_FAT_PERCENTAGE"
    private var HEIGHT = "HEIGHT"
    private var WEIGHT = "WEIGHT"
    private var STEPS = "STEPS"
    private var ACTIVE_ENERGY_BURNED = "ACTIVE_ENERGY_BURNED"
    private var HEART_RATE = "HEART_RATE"
    private var BODY_TEMPERATURE = "BODY_TEMPERATURE"
    private var BLOOD_PRESSURE_SYSTOLIC = "BLOOD_PRESSURE_SYSTOLIC"
    private var BLOOD_PRESSURE_DIASTOLIC = "BLOOD_PRESSURE_DIASTOLIC"
    private var BLOOD_OXYGEN = "BLOOD_OXYGEN"
    private var BLOOD_GLUCOSE = "BLOOD_GLUCOSE"
    private var MOVE_MINUTES = "MOVE_MINUTES"
    private var DISTANCE_DELTA = "DISTANCE_DELTA"
    private var MEDITATION = "MEDITATION"


    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_health")
            val plugin = HealthPlugin(registrar.activity(), channel)
            registrar.addActivityResultListener(plugin)
            channel.setMethodCallHandler(plugin)
        }
    }


    /// DataTypes to register
    private val fitnessOptions = FitnessOptions.builder()
            .addDataType(keyToHealthDataType(BODY_FAT_PERCENTAGE), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(HEIGHT), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(WEIGHT), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(STEPS), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(ACTIVE_ENERGY_BURNED), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(HEART_RATE), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(BODY_TEMPERATURE), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(BLOOD_PRESSURE_SYSTOLIC), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(BLOOD_OXYGEN), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(BLOOD_GLUCOSE), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(MOVE_MINUTES), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(DISTANCE_DELTA), FitnessOptions.ACCESS_READ)
            .addDataType(keyToHealthDataType(BODY_FAT_PERCENTAGE), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(HEIGHT), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(WEIGHT), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(STEPS), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(ACTIVE_ENERGY_BURNED), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(HEART_RATE), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(BODY_TEMPERATURE), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(BLOOD_PRESSURE_SYSTOLIC), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(BLOOD_OXYGEN), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(BLOOD_GLUCOSE), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(MOVE_MINUTES), FitnessOptions.ACCESS_WRITE)
            .addDataType(keyToHealthDataType(DISTANCE_DELTA), FitnessOptions.ACCESS_WRITE)
            .build()


    override fun success(p0: Any?) {
        handler?.post(
                Runnable { result?.success(p0) })
    }

    override fun notImplemented() {
        handler?.post(
                Runnable { result?.notImplemented() })
    }

    override fun error(
            errorCode: String, errorMessage: String?, errorDetails: Any?) {
        handler?.post(
                Runnable { result?.error(errorCode, errorMessage, errorDetails) })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("FLUTTER_HEALTH", "Access Granted!")
                mResult?.success(true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("FLUTTER_HEALTH", "Access Denied!")
                mResult?.success(false);
            }
        }
        return false
    }

    private var mResult: Result? = null

    private fun keyToHealthDataType(type: String): DataType {
        return when (type) {
            BODY_FAT_PERCENTAGE -> DataType.TYPE_BODY_FAT_PERCENTAGE
            HEIGHT -> DataType.TYPE_HEIGHT
            WEIGHT -> DataType.TYPE_WEIGHT
            STEPS -> DataType.TYPE_STEP_COUNT_DELTA
            ACTIVE_ENERGY_BURNED -> DataType.TYPE_CALORIES_EXPENDED
            HEART_RATE -> DataType.TYPE_HEART_RATE_BPM
            BODY_TEMPERATURE -> HealthDataTypes.TYPE_BODY_TEMPERATURE
            BLOOD_PRESSURE_SYSTOLIC -> HealthDataTypes.TYPE_BLOOD_PRESSURE
            BLOOD_PRESSURE_DIASTOLIC -> HealthDataTypes.TYPE_BLOOD_PRESSURE
            BLOOD_OXYGEN -> HealthDataTypes.TYPE_OXYGEN_SATURATION
            BLOOD_GLUCOSE -> HealthDataTypes.TYPE_BLOOD_GLUCOSE
            MOVE_MINUTES -> DataType.TYPE_MOVE_MINUTES
            DISTANCE_DELTA -> DataType.TYPE_DISTANCE_DELTA
            MEDITATION -> DataType.TYPE_DISTANCE_DELTA
            else -> DataType.TYPE_STEP_COUNT_DELTA
        }
    }

    private fun getUnit(type: String): Field {
        return when (type) {
            BODY_FAT_PERCENTAGE -> Field.FIELD_PERCENTAGE
            HEIGHT -> Field.FIELD_HEIGHT
            WEIGHT -> Field.FIELD_WEIGHT
            STEPS -> Field.FIELD_STEPS
            ACTIVE_ENERGY_BURNED -> Field.FIELD_CALORIES
            HEART_RATE -> Field.FIELD_BPM
            BODY_TEMPERATURE -> HealthFields.FIELD_BODY_TEMPERATURE
            BLOOD_PRESSURE_SYSTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC
            BLOOD_PRESSURE_DIASTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC
            BLOOD_OXYGEN -> HealthFields.FIELD_OXYGEN_SATURATION
            BLOOD_GLUCOSE -> HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
            MOVE_MINUTES -> Field.FIELD_DURATION
            DISTANCE_DELTA -> Field.FIELD_DISTANCE
            MEDITATION -> Field.FIELD_DURATION
            else -> Field.FIELD_PERCENTAGE
        }
    }

    private fun keyToFitnessActivity(type: String): String {
        return when (type) {
            MEDITATION -> FitnessActivities.MEDITATION
            else -> FitnessActivities.MEDITATION
        }
    }

    /// Extracts the (numeric) value from a Health Data Point
    private fun getHealthDataValue(dataPoint: DataPoint, unit: Field): Any {
        return try {
            dataPoint.getValue(unit).asFloat()
        } catch (e1: Exception) {
            try {
                dataPoint.getValue(unit).asInt()
            } catch (e2: Exception) {
                try {
                    dataPoint.getValue(unit).asString()
                } catch (e3: Exception) {
                    Log.e("FLUTTER_HEALTH::ERROR", e3.toString())
                }
            }
        }
    }

    /// Called when the "getHealthDataByType" is invoked from Flutter
    private fun getData(call: MethodCall, result: Result) {
        val type = call.argument<String>("dataTypeKey")!!
        val startTime = call.argument<Long>("startDate")!!
        val endTime = call.argument<Long>("endDate")!!

        // Look up data type and unit for the type key
        val dataType = keyToHealthDataType(type)
        val unit = getUnit(type)

        /// Start a new thread for doing a GoogleFit data lookup
        thread {
            try {
                val fitnessOptions = FitnessOptions.builder().addDataType(dataType).build()
                val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity.applicationContext, fitnessOptions)

                val response = Fitness.getHistoryClient(activity.applicationContext, googleSignInAccount)
                        .readData(DataReadRequest.Builder()
                                .read(dataType)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .build())

                /// Fetch all data points for the specified DataType
                val dataPoints = Tasks.await<DataReadResponse>(response).getDataSet(dataType)

                /// For each data point, extract the contents and send them to Flutter, along with date and unit.
                val healthData = dataPoints.dataPoints.mapIndexed { _, dataPoint ->
                    return@mapIndexed hashMapOf(
                            "value" to getHealthDataValue(dataPoint, unit),
                            "date_from" to dataPoint.getStartTime(TimeUnit.MILLISECONDS),
                            "date_to" to dataPoint.getEndTime(TimeUnit.MILLISECONDS),
                            "unit" to unit.toString()
                    )
                }
                activity.runOnUiThread { result.success(healthData) }
            } catch (e3: Exception) {
                activity.runOnUiThread { result.success(null) }
            }
        }
    }

    /// Called when the "writeHealthData" is invoked from Flutter
    private fun writeData(call: MethodCall, result: Result) {
        val type = call.argument<String>("dataTypeKey")!!
        val value = call.argument<Int>("value")!!
        val startTime = call.argument<Long>("startDate")!!
        val endTime = call.argument<Long>("endDate")!!

        // Look up data type and unit for the type key
        val dataType = keyToHealthDataType(type)
        val unit = getUnit(type)

        /// Start a new thread for doing a GoogleFit data lookup
        thread {
            try {
                val fitnessOptions = FitnessOptions.builder()
                    .addDataType(dataType, FitnessOptions.ACCESS_WRITE)
                    .build()
                val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity.applicationContext, fitnessOptions)
                val dataSet = prepareDataSet(dataType, unit, value, startTime, endTime)
                Fitness.getHistoryClient(activity.applicationContext, googleSignInAccount)
                    .insertData(dataSet)
                    .addOnSuccessListener {
                        Log.i("FLUTTER_HEALTH", "DataSet added successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("FLUTTER_HEALTH", "There was an error adding the DataSet", e)
                    }

                activity.runOnUiThread { result.success(null) }

            } catch (e3: Exception) {
                activity.runOnUiThread { result.success(null) }
            }
        }
    }

    private fun prepareDataSet(dataType: DataType, field: Field, value: Int, startTime: Long, endTime: Long): DataSet {

        // Create a data source
        val dataSource = DataSource.Builder()
            .setAppPackageName(activity.applicationContext.packageName)
            .setDataType(dataType)
            .setType(DataSource.TYPE_RAW)
            .build()

        // For each data point, specify a start time, end time, and the data value
        // -- in this case, the number of new steps.
        val dataPoint =
            DataPoint.builder(dataSource)
                .setField(field, value)
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

        return DataSet.builder(dataSource)
            .add(dataPoint)
            .build()
    }

    /// Called when the "writeHealthDataSession" is invoked from Flutter
    private fun writeSessionData(call: MethodCall, result: Result) {
        val type = call.argument<String>("dataTypeKey")!!
        val name = call.argument<String>("name")!!
        val startTime = call.argument<Long>("startDate")!!
        val endTime = call.argument<Long>("endDate")!!

        // Look up data type and fitnessActivity for the type key
        val dataType = keyToHealthDataType(type)
        val fitnessActivity = keyToFitnessActivity(type)

        /// Start a new thread for doing a GoogleFit data lookup
        thread {
            try {
                val fitnessOptions = FitnessOptions.builder()
                    .addDataType(dataType, FitnessOptions.ACCESS_WRITE)
                    .build()
                val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity.applicationContext, fitnessOptions)
                val identifier = activity.applicationContext.packageName + googleSignInAccount.id + startTime.toString()
                val session = Session.Builder()
                    .setName(name)
                    .setIdentifier(identifier)
                    .setStartTime(startTime, TimeUnit.MILLISECONDS)
                    .setEndTime(endTime, TimeUnit.MILLISECONDS)
                    .setActivity(fitnessActivity)
                    .build()

                val insertTask =
                    Fitness.getSessionsClient(activity.applicationContext, googleSignInAccount)
                            .insertSession(SessionInsertRequest.Builder()
                                .setSession(session)
                                .build())
                            .addOnSuccessListener {
                                Log.i("FLUTTER_HEALTH", "Session inserted successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("FLUTTER_HEALTH", "There was an error adding the session", e)
                            }

                Tasks.await(insertTask);

                activity.runOnUiThread { result.success(null) }

            } catch (e3: Exception) {
                activity.runOnUiThread { result.success(null) }
            }
        }
    }

    /// Called when the "requestAuthorization" is invoked from Flutter 
    private fun requestAuthorization(call: MethodCall, result: Result) {
        val optionsToRegister = callToHealthTypes(call)
        mResult = result

        val isGranted = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)

        /// Not granted? Ask for permission
        if (!isGranted) {
            GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    optionsToRegister)
        }
        /// Permission already granted
        else {
            mResult?.success(true)
        }
    }

    private fun callToHealthTypes(call: MethodCall): FitnessOptions {
        val typesBuilder = FitnessOptions.builder()
        val args = call.arguments as HashMap<*, *>
        val types = args["types"] as ArrayList<*>
        for (typeKey in types) {
            if (typeKey !is String) continue
            typesBuilder.addDataType(keyToHealthDataType(typeKey), FitnessOptions.ACCESS_READ)
            typesBuilder.addDataType(keyToHealthDataType(typeKey), FitnessOptions.ACCESS_WRITE)
        }
        return typesBuilder.build()
    }

    /// Handle calls from the MethodChannel
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "requestAuthorization" -> requestAuthorization(call, result)
            "getData" -> getData(call, result)
            "writeData" -> writeData(call, result)
            "writeSessionData" -> writeSessionData(call, result)
            else -> result.notImplemented()
        }
    }
}
