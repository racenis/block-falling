package com.example.myapplication

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import androidx.core.view.GestureDetectorCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        Log.d("AAA", "Starting")
        gLView = FieldSurfaceView(this)
        setContentView(gLView)
    }
}

class FieldSurfaceView(context: Context) : GLSurfaceView(context), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private var gestureDetector: GestureDetectorCompat

    private var dragged = false
    private var dragging = 0

    init {
        setEGLContextClientVersion(3)

        setRenderer(FieldRenderer())

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        gestureDetector = GestureDetectorCompat(context, this)
        gestureDetector.setOnDoubleTapListener(this)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(e)) {
            true
        } else {
            super.onTouchEvent(e)
        }
    }

    override fun onDown(event: MotionEvent): Boolean {
        dragging = 0
        dragged = false
        return true
    }

    override fun onFling (event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(velocityX) > 5000 || abs(velocityY) > 5000) {
            if (abs(velocityX) > abs(velocityY)) {
                if (velocityX > 0) {
                    Application.Tetromino.rotateRight()
                    requestRender()
                } else {
                    Application.Tetromino.rotateLeft()
                    requestRender()
                }
            } else {
                if (velocityY > 0) {
                    Application.Tetromino.rotateDown()
                    requestRender()
                } else {
                    Application.Tetromino.rotateUp()
                    requestRender()
                }
            }
        }
        return true
    }

    override fun onScroll (event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        dragging++
        if (/*!dragged &&*/ (dragging > 10) && (abs(distanceX) > 2 || abs(distanceY) > 2)) {
            dragged = true
            dragging = -10
            if (abs(distanceX) > abs(distanceY)) {
                if (distanceX > 0) {
                    Application.Tetromino.moveLeft()
                    requestRender()
                } else {
                    Application.Tetromino.moveRight()
                    requestRender()
                }
            } else {
                if (distanceY > 0) {
                    Application.Tetromino.moveUp()
                    requestRender()
                } else {
                    Application.Tetromino.moveDown()
                    requestRender()
                }
            }
        }
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Application.Tetromino.dropDown()
        requestRender()
        return true
    }

    override fun onShowPress(event: MotionEvent) = Unit
    override fun onLongPress(event: MotionEvent) = Unit
    override fun onSingleTapUp(event: MotionEvent): Boolean = true
    override fun onDoubleTapEvent(event: MotionEvent): Boolean = true
    override fun onSingleTapConfirmed(event: MotionEvent): Boolean = true
}

class FieldRenderer : GLSurfaceView.Renderer {
    private val projMatrix = FloatArray(16)
    private val scaleMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val scaleViewMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)



    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(Application.backgroundColor[0], Application.backgroundColor[1], Application.backgroundColor[2], 1.0f)

        GLES30.glEnable( GLES30.GL_DEPTH_TEST );
        GLES30.glDepthFunc( GLES30.GL_LEQUAL );
        GLES30.glDepthMask( true );

        GLES30.glEnable(GLES30.GL_CULL_FACE)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        Application.draw(projMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.scaleM(scaleMatrix, 0, 1.9f, 1.9f, 1.0f)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.5f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0.5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(scaleViewMatrix, 0, viewMatrix, 0, scaleMatrix, 0)
        Matrix.multiplyMM(projMatrix, 0, projectionMatrix, 0, scaleViewMatrix, 0)
    }

    

}


fun loadShader(vertexCode: String, fragmentCode: String): Int {
    val vertex = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
    val fragment = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)

    GLES30.glShaderSource(vertex, vertexCode)
    GLES30.glCompileShader(vertex)
    Log.d("SHH", GLES30.glGetProgramInfoLog(vertex))
    GLES30.glShaderSource(fragment, fragmentCode)
    GLES30.glCompileShader(fragment)
    Log.d("SHH", GLES30.glGetProgramInfoLog(fragment))

    val shader = GLES30.glCreateProgram()

    GLES30.glAttachShader(shader, vertex)
    GLES30.glAttachShader(shader, fragment)
    GLES30.glLinkProgram(shader)
    Log.d("SHH", GLES30.glGetProgramInfoLog(shader))

    return shader
}

object Application {
    object Field {
        var width: Int = 5
        var length: Int = 5
        var depth: Int  = 7
        var fieldUnit: Float = 0.0f
        var cornerX: Float = 0.0f
        var cornerY: Float = 0.0f
        var cornerZ: Float = 0.0f
        lateinit var field: Array<Array<BooleanArray>>
        lateinit var fieldColors: Array<FloatArray>
        lateinit var fieldColorModifers: Array<Array<Array<Array<FloatArray>>>>

        fun pointIsInside(x: Int, y: Int, z: Int) = x >= 0 && y >= 0 && z >= 0 && x < width && y < length //&& z < depth
        fun pointIsCollide(x: Int, y: Int, z: Int) = if (z < depth) field[x][y][z] else false
        fun fieldAppend(x: Int, y: Int, z: Int) { field[x][y][z] = true }
        fun fieldCheckBottomLayer() {
            for (i in 0 until width)
                for (j in 0 until length)
                    if(!field[i][j][0]) return

            for (i in 0 until width)
                for (j in 0 until length)
                    for (k in 0 until depth-1)
                        field[i][j][k] = field[i][j][k+1]
        }
        fun fieldReset() {
            fieldUnit = 0.98f/maxOf(width, length, depth).toFloat()
            cornerX = 0.0f - ((fieldUnit * width.toFloat()) / 2.0f)
            cornerY = 0.0f - ((fieldUnit * length.toFloat()) / 2.0f)
            cornerZ = -1.0f
            field = Array(width) { Array(length) { BooleanArray(depth) { false } } }
            fieldColors = Array(depth) { floatArrayOf( Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) }
            fieldColorModifers = Array(width) { Array(length) { Array(depth) { Array(8)
            { floatArrayOf((Random.nextFloat()-0.5f)/3.0f, (Random.nextFloat()-0.5f)/3.0f, (Random.nextFloat()-0.5f)/3.0f) } } } }
        }

        init {
            fieldReset()
        }

        lateinit var vertices: FloatArray
        lateinit var colors: FloatArray
        lateinit var backgroundLines: FloatArray

        fun assembleVertexArrays() {
            Log.d("AAA", "Assembling field vertices")
            var verts: MutableList<Float> = mutableListOf()
            var colrs: MutableList<Float> = mutableListOf()

            for (i in 0 until width)
            for (j in 0 until length)
            for (k in 0 until depth){
                if(field[i][j][k]){
                    //Log.d("AAA", "Found $i $j $k")
                    val c_left: Float = cornerX + (fieldUnit * i.toFloat())
                    val c_right: Float = cornerX + (fieldUnit * (i+1).toFloat())
                    val c_lower: Float = cornerY + (fieldUnit * j.toFloat())
                    val c_upper: Float = cornerY + (fieldUnit * (j+1).toFloat())
                    val c_bottom: Float = cornerZ + (fieldUnit * k.toFloat())
                    val c_top: Float = cornerZ + (fieldUnit * (k+1).toFloat())
                    val col = { d:Int ->
                        colrs.add((fieldColors[k][0] + fieldColorModifers[i][j][k][d][0]).coerceIn(0.0f, 1.0f));
                        colrs.add((fieldColors[k][1] + fieldColorModifers[i][j][k][d][1]).coerceIn(0.0f, 1.0f));
                        colrs.add((fieldColors[k][2] + fieldColorModifers[i][j][k][d][2]).coerceIn(0.0f, 1.0f));
                        colrs.add(1.0f)}
                    val tlf = {verts.add(c_left); verts.add(c_upper); verts.add(c_top); col(0)}
                    val trf = {verts.add(c_right); verts.add(c_upper); verts.add(c_top); col(1)}
                    val blf = {verts.add(c_left); verts.add(c_lower); verts.add(c_top); col(2)}
                    val brf = {verts.add(c_right); verts.add(c_lower); verts.add(c_top); col(3)}
                    val tlb = {verts.add(c_left); verts.add(c_upper); verts.add(c_bottom); col(4)}
                    val trb = {verts.add(c_right); verts.add(c_upper); verts.add(c_bottom); col(5)}
                    val blb = {verts.add(c_left); verts.add(c_lower); verts.add(c_bottom); col(6)}
                    val brb = {verts.add(c_right); verts.add(c_lower); verts.add(c_bottom); col(7)}

                    // virsējais
                    tlf(); blf(); brf(); brf(); trf(); tlf()

                    // augšējais
                    trf(); trb(); tlb(); tlb(); tlf(); trf()

                    // apakšējais
                    brf(); blf(); blb(); blb(); brb(); brf()

                    // kreisais
                    tlf(); tlb(); blb(); blb(); blf(); tlf()

                    // labais
                    trf(); brf(); brb(); brb(); trb(); trf()
                }
            }

            Log.d("AAA", "Done assembling")
            vertices = verts.toFloatArray()
            colors = colrs.toFloatArray()
        }

        fun assembleBackgroundLines() {
            var lines: MutableList<Float> = mutableListOf()

            for (i in 0..width) {
                // augša
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY)
                lines.add(cornerZ + fieldUnit*depth.toFloat())

                // 2x apakša
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY)
                lines.add(cornerZ)
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY)
                lines.add(cornerZ)

                // 2x apakša, otra puse
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY + fieldUnit*length.toFloat())
                lines.add(cornerZ)
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY + fieldUnit*length.toFloat())
                lines.add(cornerZ)

                // augša, otra puse
                lines.add(cornerX + fieldUnit*i.toFloat())
                lines.add(cornerY + fieldUnit*length.toFloat())
                lines.add(cornerZ + fieldUnit*depth.toFloat())
            }

            for (i in 0..length) {
                // augša
                lines.add(cornerX)
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ + fieldUnit*depth.toFloat())

                // 2x apakša
                lines.add(cornerX)
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ)
                lines.add(cornerX)
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ)

                // 2x apakša, otra puse
                lines.add(cornerX + fieldUnit*width.toFloat())
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ)
                lines.add(cornerX + fieldUnit*width.toFloat())
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ)

                // augša, otra puse
                lines.add(cornerX + fieldUnit*width.toFloat())
                lines.add(cornerY + fieldUnit*i.toFloat())
                lines.add(cornerZ + fieldUnit*depth.toFloat())
            }

            for (i in 0..depth) {
                var c = { lines.add(cornerX); lines.add(cornerY); lines.add(cornerZ + fieldUnit*i.toFloat()); }
                var cw = { lines.add(cornerX + fieldUnit*width.toFloat()); lines.add(cornerY); lines.add(cornerZ + fieldUnit*i.toFloat()); }
                var ch = { lines.add(cornerX); lines.add(cornerY + fieldUnit*length.toFloat()); lines.add(cornerZ + fieldUnit*i.toFloat()); }
                var cwh = { lines.add(cornerX + fieldUnit*width.toFloat()); lines.add(cornerY + fieldUnit*length.toFloat()); lines.add(cornerZ + fieldUnit*i.toFloat()); }

                c(); cw(); c(); ch(); cwh(); cw(); cwh(); ch()
            }

            backgroundLines = lines.toFloatArray()
        }
    }

    object Tetromino {
        data class TetrominoSegment(val x: Int, val y: Int){
            var rotated = FloatArray(4)
            fun applyRotation(matrix: FloatArray){
                val r = FloatArray(4)
                r[0] = x.toFloat()
                r[1] = y.toFloat()
                r[2] = 0.0f
                r[3] = 1.0f

                Matrix.multiplyMV(rotated, 0, matrix, 0, r, 0)
            }

            val transfX
                get() = locationX + rotated[0].roundToInt()

            val transfY
                get() = locationY + rotated[1].roundToInt()

            val transfZ
                get() = locationZ + rotated[2].roundToInt()

        }
        val TetrominoType = listOf(
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(1, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(0, 1),
                TetrominoSegment(1, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(0, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(2, 0)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(0, 1),
                TetrominoSegment(1, 1)
            )
        )

        lateinit var current: List<TetrominoSegment>
        var locationX: Int = 0
        var locationY: Int = 0
        var locationZ: Int = 0
        var rotationMatrix = FloatArray(16)

        fun reset() {
            locationX = Field.width / 2
            locationY = Field.length / 2
            locationZ = Field.depth - 1
            Matrix.setIdentityM(rotationMatrix, 0)
            current = TetrominoType.random()
            if (!isLocationRotationValid()) Field.fieldReset();
        }

        init {
            reset()
        }


        fun rotateUp() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateDown() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, -90.0f, 0.0f, 1.0f, 0.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateLeft() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateRight() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, -90.0f, 0.0f, 0.0f, 1.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun moveLeft() {
            val oldLocation = locationX
            locationX--
            if(!isLocationRotationValid()) locationX = oldLocation
        }
        fun moveRight() {
            val oldLocation = locationX
            locationX++
            if(!isLocationRotationValid()) locationX = oldLocation
        }
        fun moveUp() {
            val oldLocation = locationY
            locationY++
            if(!isLocationRotationValid()) locationY = oldLocation
        }
        fun moveDown() {
            val oldLocation = locationY
            locationY--
            if(!isLocationRotationValid()) locationY = oldLocation
        }
        fun dropDown() {
            val oldLocation = locationZ
            locationZ--
            if(!isLocationRotationValid()) {
                locationZ = oldLocation
                for (t in current) Field.fieldAppend(t.transfX, t.transfY, t.transfZ)
                Field.fieldCheckBottomLayer()
                reset()
            }
        }

        fun isLocationRotationValid(): Boolean {
            for (t in current) {
                t.applyRotation(rotationMatrix)
                if (
                    !Field.pointIsInside(t.transfX, t.transfY, t.transfZ) ||
                    Field.pointIsCollide(t.transfX, t.transfY, t.transfZ)
                ) return false
            }
            return true
        }

        lateinit var vertices: FloatArray

        fun assembleVertexArrays() {
            Log.d("AAA", "Assembling tetromino vertices")
            var verts: MutableList<Float> = mutableListOf()

            for (t in current){
                t.applyRotation(rotationMatrix)

                val c_left: Float = Field.cornerX + (Field.fieldUnit * t.transfX.toFloat())
                val c_right: Float = Field.cornerX + (Field.fieldUnit * (t.transfX+1).toFloat())
                val c_lower: Float = Field.cornerY + (Field.fieldUnit * t.transfY.toFloat())
                val c_upper: Float = Field.cornerY + (Field.fieldUnit * (t.transfY+1).toFloat())
                val c_bottom: Float = Field.cornerZ + (Field.fieldUnit * t.transfZ.toFloat())
                val c_top: Float = Field.cornerZ + (Field.fieldUnit * (t.transfZ+1).toFloat())
                val tlf = {verts.add(c_left); verts.add(c_upper); verts.add(c_top)}
                val trf = {verts.add(c_right); verts.add(c_upper); verts.add(c_top)}
                val blf = {verts.add(c_left); verts.add(c_lower); verts.add(c_top)}
                val brf = {verts.add(c_right); verts.add(c_lower); verts.add(c_top)}
                val tlb = {verts.add(c_left); verts.add(c_upper); verts.add(c_bottom)}
                val trb = {verts.add(c_right); verts.add(c_upper); verts.add(c_bottom)}
                val blb = {verts.add(c_left); verts.add(c_lower); verts.add(c_bottom)}
                val brb = {verts.add(c_right); verts.add(c_lower); verts.add(c_bottom)}

                // augša
                tlf(); trf(); trf(); brf(); brf(); blf(); blf(); tlf()

                // apakša
                tlb(); trb(); trb(); brb(); brb(); blb(); blb(); tlb()

                // malas
                tlf(); tlb(); trf(); trb(); brf(); brb(); blf(); blb()

            }

            Log.d("AAA", "Done assembling")
            vertices = verts.toFloatArray()
        }

    }

    private val vertexShaderCode =
        "uniform mat4 projMatrix;" +
                "attribute vec4 vertPosition;" +
                "attribute vec4 vColor;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_Position = projMatrix * vertPosition;" +
                "  vertColor = vColor;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_FragColor = vertColor;" +
                "}"

    private val lineVertexShaderCode =
        "uniform mat4 projMatrix;" +
                "attribute vec4 vertPosition;" +
                "void main() {" +
                "  gl_Position = projMatrix * vertPosition;" +
                "}"

    private val lineFragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vertColor;" +
                "void main() {" +
                "  gl_FragColor = vertColor;" +
                "}"


    val tetrominoColor = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f)
    val backgroundLineColor = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)
    val backgroundColor  = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)


    var fieldShader: Int = loadShader(vertexShaderCode, fragmentShaderCode)
    var lineShader: Int = loadShader(lineVertexShaderCode, lineFragmentShaderCode)

    var positionIndex: Int = 0
    var colorIndex: Int = 0
    var projMatrixIndex: Int = 0

    val vertexStride: Int = 3 * 4
    val colorVertexStride: Int = 4 * 4

    fun draw(projMatrix: FloatArray) {
        Log.d("AAA", "Starting the draw")
        Field.assembleVertexArrays()
        Field.assembleBackgroundLines()
        Tetromino.assembleVertexArrays()

        val triangles = Field.vertices
        val colors = Field.colors
        val lines = Tetromino.vertices

        val backgroundLines = Field.backgroundLines

        Log.d("AAA", "Making buffers")
        var vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(triangles.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(triangles)
                    position(0)
                }
            }

        var vertexColorBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(colors.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(colors)
                    position(0)
                }
            }

        var lineVertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(lines.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(lines)
                    position(0)
                }
            }

        var backgroundLineVertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(backgroundLines.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(backgroundLines)
                    position(0)
                }
            }

        Log.d("AAA", "Done making buffers")


        // zīmēt lauciņa kastes
        GLES30.glUseProgram(fieldShader)

        positionIndex = GLES30.glGetAttribLocation(fieldShader, "vertPosition")
        GLES30.glEnableVertexAttribArray(positionIndex)
        GLES30.glVertexAttribPointer(
            positionIndex,
            3, // xyz koordināes
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        colorIndex = GLES30.glGetAttribLocation(fieldShader, "vColor")
        GLES30.glEnableVertexAttribArray(colorIndex)
        GLES30.glVertexAttribPointer(
            colorIndex,
            4, // krāsas kā 4 komponenšu vektors
            GLES30.GL_FLOAT,
            false,
            colorVertexStride,
            vertexColorBuffer
        )



        projMatrixIndex = GLES30.glGetUniformLocation(fieldShader, "projMatrix")
        GLES30.glUniformMatrix4fv(projMatrixIndex, 1, false, projMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangles.size / 3)

        GLES30.glDisableVertexAttribArray(colorIndex)
        GLES30.glDisableVertexAttribArray(positionIndex)




        // zīmēt laukuma fonu
        GLES30.glUseProgram(lineShader)

        positionIndex = GLES30.glGetAttribLocation(lineShader, "vertPosition")
        GLES30.glEnableVertexAttribArray(positionIndex)
        GLES30.glVertexAttribPointer(
            positionIndex,
            3, // xyz koordināes
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            backgroundLineVertexBuffer
        )

        colorIndex = GLES30.glGetUniformLocation(lineShader, "vertColor")
        GLES30.glUniform4fv(colorIndex, 1, backgroundLineColor, 0)


        projMatrixIndex = GLES30.glGetUniformLocation(lineShader, "projMatrix")
        GLES30.glUniformMatrix4fv(projMatrixIndex, 1, false, projMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, backgroundLines.size / 3)

        GLES30.glDisableVertexAttribArray(positionIndex)


        // zīmēt liekamo kauliņu
        GLES30.glDisable( GLES30.GL_DEPTH_TEST );
        positionIndex = GLES30.glGetAttribLocation(lineShader, "vertPosition")
        GLES30.glEnableVertexAttribArray(positionIndex)
        GLES30.glVertexAttribPointer(
            positionIndex,
            3, // xyz koordināes
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            lineVertexBuffer
        )

        colorIndex = GLES30.glGetUniformLocation(lineShader, "vertColor")
        GLES30.glUniform4fv(colorIndex, 1, tetrominoColor, 0)


        projMatrixIndex = GLES30.glGetUniformLocation(lineShader, "projMatrix")
        GLES30.glUniformMatrix4fv(projMatrixIndex, 1, false, projMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, lines.size / 3)

        GLES30.glDisableVertexAttribArray(positionIndex)
        GLES30.glEnable( GLES30.GL_DEPTH_TEST );

    }


    }

