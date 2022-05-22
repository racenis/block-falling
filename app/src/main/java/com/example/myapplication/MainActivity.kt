package com.example.myapplication

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        Log.d("AAA", "Starting")
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)




        /*val benis: Button = findViewById(R.id.yes_button)
        benis.setOnClickListener {
            val bimage: ImageView = findViewById(R.id.indian_image)
            bimage.visibility = View.VISIBLE

            val btext: TextView = findViewById(R.id.r_u_gay_text)
            btext.visibility = View.GONE

            benis.visibility = View.GONE

        }*/
    }
}



class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3)

        renderer = MyGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var mTriangle: Triangle
    //private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)



    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)

        GLES30.glEnable( GLES30.GL_DEPTH_TEST );
        GLES30.glDepthFunc( GLES30.GL_LEQUAL );
        GLES30.glDepthMask( true );

        GLES30.glEnable(GLES30.GL_CULL_FACE)

        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        //mSquare = Square2()




    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        mTriangle.draw(vPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    

}


fun loadShader(vertexCode: String, fragmentCode: String): Int {
    val vertex = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
    val fragment = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)

    GLES30.glShaderSource(vertex, vertexCode)
    GLES30.glCompileShader(vertex)
    GLES30.glShaderSource(fragment, fragmentCode)
    GLES30.glCompileShader(fragment)

    val shader = GLES30.glCreateProgram()

    GLES30.glAttachShader(shader, vertex)
    GLES30.glAttachShader(shader, fragment)
    GLES30.glLinkProgram(shader)

    return shader
}




// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
)

var triangleColors = floatArrayOf(
    1.0f, 0.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
)

class Triangle {

    object Field {
        val width: Int = 5
        val length: Int = 5
        val depth: Int = 7
        val fieldUnit: Float  = 0.15f
        val cornerX: Float  = 0.0f - ((fieldUnit * width.toFloat()) / 2.0f)
        val cornerY: Float  = 0.0f - ((fieldUnit * length.toFloat()) / 2.0f)
        val cornerZ: Float  = -1.0f
        var field = Array(width) { Array(length) { BooleanArray(depth) { false } } }

        init {
            field[0][0][0] = true
            field[4][0][0] = true
            field[0][4][0] = true
            field[4][4][0] = true

            field[0][0][1] = true
            field[4][0][1] = true
            field[0][4][1] = true
            field[4][4][1] = true

            field[0][0][2] = true
            field[4][0][2] = true
            field[0][4][2] = true
            field[4][4][2] = true




            field[2][2][0] = true
        }

        lateinit var vertices: FloatArray
        lateinit var colors: FloatArray
        lateinit var textures: FloatArray

        fun assembleVertexArrays() {
            Log.d("AAA", "Assembling field vertices")
            var verts: MutableList<Float> = mutableListOf()
            var colrs: MutableList<Float> = mutableListOf()

            for (i in 0 until width)
            for (j in 0 until length)
            for (k in 0 until depth){
                Log.d("AAA", "Checking $i $j $k")
                if(field[i][j][k]){
                    Log.d("AAA", "Found $i $j $k")
                    val c_left: Float = cornerX + (fieldUnit * i.toFloat())
                    val c_right: Float = cornerX + (fieldUnit * (i+1).toFloat())
                    val c_lower: Float = cornerY + (fieldUnit * j.toFloat())
                    val c_upper: Float = cornerY + (fieldUnit * (j+1).toFloat())
                    val c_bottom: Float = cornerZ + (fieldUnit * k.toFloat())
                    val c_top: Float = cornerZ + (fieldUnit * (k+1).toFloat())
                    val col = {colrs.add(Random.nextFloat()); colrs.add(Random.nextFloat()); colrs.add(Random.nextFloat()); colrs.add(1.0f)}
                    val tlf = {verts.add(c_left); verts.add(c_upper); verts.add(c_top); col()}
                    val trf = {verts.add(c_right); verts.add(c_upper); verts.add(c_top); col()}
                    val blf = {verts.add(c_left); verts.add(c_lower); verts.add(c_top); col()}
                    val brf = {verts.add(c_right); verts.add(c_lower); verts.add(c_top); col()}
                    val tlb = {verts.add(c_left); verts.add(c_upper); verts.add(c_bottom); col()}
                    val trb = {verts.add(c_right); verts.add(c_upper); verts.add(c_bottom); col()}
                    val blb = {verts.add(c_left); verts.add(c_lower); verts.add(c_bottom); col()}
                    val brb = {verts.add(c_right); verts.add(c_lower); verts.add(c_bottom); col()}

                    // virsējais
                    tlf()
                    blf()
                    brf()
                    brf()
                    trf()
                    tlf()

                    // augšējais
                    trf()
                    trb()
                    tlb()
                    tlb()
                    tlf()
                    trf()

                    // apakšējais

                    brf()
                    blf()
                    blb()
                    blb()
                    brb()
                    brf()

                    // kreisais
                    tlf()
                    tlb()
                    blb()
                    blb()
                    blf()
                    tlf()

                    // labais
                    trf()
                    brf()
                    brb()
                    brb()
                    trb()
                    trf()
                }
            }

            Log.d("AAA", "Done assembling")
            vertices = verts.toFloatArray()
            colors = colrs.toFloatArray()
        }
    }

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec4 veColor;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  vertColor = veColor;" +
                "}"

    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0


    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_FragColor = vertColor;" +
                "}"







    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)




    private var mProgram: Int = loadShader(vertexShaderCode, fragmentShaderCode)

    var positionHandle: Int = 0
    var colorHandle: Int = 0
    var mColorHandle: Int = 0

    val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    val colorVertexStride: Int = 4 * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        Log.d("AAA", "Starting the draw")
        Field.assembleVertexArrays()
        val triangles = Field.vertices
        //val colors = FloatArray(triangles.size / 3 * 4) {1.0f}
        val colors = Field.colors

        Log.d("AAA", "Making buffers")
        var vertexBuffer: FloatBuffer =
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(triangles.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(triangles)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }

        var vertexColorBuffer: FloatBuffer =
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(colors.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(colors)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }

        Log.d("AAA", "Done making buffers")


        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        colorHandle = GLES30.glGetAttribLocation(mProgram, "veColor")
        GLES30.glEnableVertexAttribArray(colorHandle)
        GLES30.glVertexAttribPointer(
            colorHandle,
            4, // krāsas kā 4 komponenšu vektors
            GLES30.GL_FLOAT,
            false,
            colorVertexStride,
            vertexColorBuffer
        )



        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

            // Set color for drawing the triangle
            GLES30.glUniform4fv(colorHandle, 1, color, 0)
        }

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)


        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangles.size / 3)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(colorHandle)
        GLES30.glDisableVertexAttribArray(positionHandle)

    }


    }

