(ns phys-world.shader
  (:import [org.lwjgl.opengl GL11 GL20]
           [org.lwjgl.util.vector Vector4f]
           [org.lwjgl BufferUtils]
           [java.nio.charset Charset])
  (:require [clojure.java.io :as io]))

(def program (atom nil))

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
    (GL20/glShaderSource vertexShaderId (toByteBuffer (slurp (io/file (io/resource "phys_world/shader.vs")))))
    (GL20/glCompileShader vertexShaderId)
    (when (= (GL20/glGetShader vertexShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE)
      (printLogInfo vertexShaderId)
      (System/exit -1))
    (GL20/glAttachShader program vertexShaderId)))

(defn setup-fragment-shader [program]
  (let [fragmentShaderId (GL20/glCreateShader GL20/GL_FRAGMENT_SHADER)]
    (GL20/glShaderSource fragmentShaderId (toByteBuffer (slurp (io/file (io/resource "phys_world/shader.fs")))))
    (GL20/glCompileShader fragmentShaderId)
    (when (= (GL20/glGetShader fragmentShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE) 
      (printLogInfo fragmentShaderId)
      (System/exit -1))
    (GL20/glAttachShader program fragmentShaderId)))

(defn setup []
  (swap! program (fn [_] (GL20/glCreateProgram)))
  (setup-vertex-shader @program)
  (setup-fragment-shader @program)
  (GL20/glLinkProgram @program)
  (GL20/glValidateProgram @program))

(defmacro with-program [& commands]
  `(do
     (GL20/glUseProgram @program)
     ~@commands
     (GL20/glUseProgram 0)))
