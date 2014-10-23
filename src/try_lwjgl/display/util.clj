(ns try-lwjgl.display.util
  (:import [org.lwjgl.opengl GL11 Display]
           [org.lwjgl.util.glu GLU]))

(defmacro with-pushed-attrib [attrib & commands]
  `(do
     (GL11/glPushAttrib ~attrib)
     ~@commands
     (GL11/glPopAttrib)))

(defmacro with-pushed-matrix [& commands]
  `(do
     (GL11/glPushMatrix)
     ~@commands
     (GL11/glPopMatrix)))

(defmacro do-shape [type & commands]
  `(do
    (GL11/glBegin ~type)
    ~@commands
    (GL11/glEnd)))

(defn exit-on-gl-error [errorMessage]
  (let [errorValue (GL11/glGetError)]
    (if (not (=  errorValue GL11/GL_NO_ERROR))
      (let [errorString (GLU/gluErrorString errorValue)]
        (.println System/err (str  "ERROR - " errorMessage ": " errorString))
        (when (Display/isCreated) (Display/destroy))
        (System/exit -1)))))
