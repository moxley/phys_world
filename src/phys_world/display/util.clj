(ns phys-world.display.util
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


(defn next-vertex [a b]
  ;; (partition 2 2 (interleave a b))

  (loop [n 0
         found-xor? false
         res []]
    (if (>= n (count a))
        res
        (let [an (nth a n)
              bn (nth b n)]
          (if (or (= an bn) found-xor?)
            (recur (inc n)
                   found-xor?
                   (conj res an))
            (recur (inc n)
                   true
                   (conj res bn)))))))

(defn rect-vertices [a b]
  [a (next-vertex a b)
   (next-vertex a b) b
   b (next-vertex b a)
   (next-vertex b a) a])
