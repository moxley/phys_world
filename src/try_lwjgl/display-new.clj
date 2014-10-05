(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11 GL15 GL20]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [org.lwjgl BufferUtils]
           [java.awt Font]
           [java.nio.charset Charset]
           [org.newdawn.slick Color TrueTypeFont])
  (:require [try-lwjgl.logic :as logic]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def program (atom nil))

(defn exitOnGLError [errorMessage]
  (let [errorValue (GL11/glGetError)]
    (if (not (=  errorValue GL11/GL_NO_ERROR))
      (let [errorString (GLU/gluErrorString errorValue)]
        (.println System/err (str  "ERROR - " errorMessage ": " errorString))
        (when (Display/isCreated) (Display/destroy))
        (System/exit -1)))))

(defn printLogInfo [obj]
  (let [infoLog (BufferUtils/createByteBuffer 2048)
        lengthBuffer (BufferUtils/createIntBuffer 1)
        _ (GL20/glGetShaderInfoLog obj lengthBuffer infoLog)
        infoBytes (byte-array (.get lengthBuffer))]
    (.get infoLog infoBytes)
    (if (= (count infoBytes) 0)
      nil ; return
      (.println System/err (String. infoBytes (Charset/forName "ISO-8859-1"))))))

(defn toByteBuffer [data]
  (let [vertexShaderData (.getBytes data (Charset/forName "ISO-8859-1"))
        vertexShader (BufferUtils/createByteBuffer (count vertexShaderData))]
    (.put vertexShader vertexShaderData)
    (.flip vertexShader)
    vertexShader))

(defn setup-vertex-shader [program]
  (let [vertexShaderId (GL20/glCreateShader GL20/GL_VERTEX_SHADER)]
    (GL20/glShaderSource vertexShaderId (toByteBuffer (slurp (io/file (io/resource "try_lwjgl/shader.vs")))))
    (GL20/glCompileShader vertexShaderId)
    (when (= (GL20/glGetShader vertexShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE)
      (printLogInfo vertexShaderId)
      (System/exit -1))
    (GL20/glAttachShader program vertexShaderId)))

(defn setup-fragment-shader [program]
  (let [fragmentShaderId (GL20/glCreateShader GL20/GL_FRAGMENT_SHADER)]
    (GL20/glShaderSource fragmentShaderId (toByteBuffer (slurp (io/file (io/resource "try_lwjgl/shader.fs")))))
    (GL20/glCompileShader fragmentShaderId)
    (when (= (GL20/glGetShader fragmentShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE) 
      (printLogInfo fragmentShaderId)
      (System/exit -1))
    (GL20/glAttachShader program fragmentShaderId)))

(defn setup-opengl [width height title]
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create)
  (println "OpenGL Version:" (GL11/glGetString GL11/GL_VERSION))

  (swap! program (fn [_] (GL20/glCreateProgram)))

  ;; Create and attach shaders to program
  (setup-vertex-shader @program)
  (setup-fragment-shader @program)

  (GL20/glLinkProgram @program)
  (GL20/glValidateProgram @program)
  (exitOnGLError "Error in setupOpenGL"))

(defn draw []
  (GL20/glUseProgram @program)
  
  (GL11/glBegin GL11/GL_TRIANGLES)
  (GL11/glColor3f 1 0 0)
  (GL11/glVertex2f (float -0.5) (float -0.5))
  (GL11/glColor3f 0 1 0)
  (GL11/glVertex2f (float 0.5) (float -0.5))
  (GL11/glColor3f 0 0 1)
  (GL11/glVertex2f 0 (float 0.5))
  (GL11/glEnd)
  
  (GL20/glUseProgram 0)
  (exitOnGLError "Error in draw"))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha"))
