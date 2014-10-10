(ns try-lwjgl.model.axes
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]))

(def AXIS-WIDTH 0.05)

(defn draw []
  (shader/with-program
    ;; Height markers
    (doseq [n (range 10)]
      (util/with-pushed-matrix
        ;; x-axis (red)
        (GL11/glColor3f 1 0 0)
        (GL11/glTranslatef 0 n 0)
        (util/do-shape GL11/GL_LINE_STRIP
                  (GL11/glVertex3f -10 0 0)
                  (GL11/glVertex3f 10 0 0))))


    (util/with-pushed-matrix

     (GL11/glScaled 4.0 4.0 4.0)

     ;; x-axis (red)
     (GL11/glColor3f 1.0 0.0 0.0)
     (util/do-shape GL11/GL_QUADS
               (GL11/glVertex3f -1.0 0.0 0.0)
               (GL11/glVertex3f  1.0 0.0 0.0)
               (GL11/glVertex3f  1.0 AXIS-WIDTH 0.0)
               (GL11/glVertex3f -1.0 AXIS-WIDTH 0.0))

     ;; y-axis (green)
     (GL11/glColor3f 0.0 1.0 0.0)
     (util/do-shape GL11/GL_QUADS
               (GL11/glVertex3f 0.0 -1.0 0.0)
               (GL11/glVertex3f 0.0  1.0 0.0)
               (GL11/glVertex3f 0.0  1.0 AXIS-WIDTH)
               (GL11/glVertex3f 0.0 -1.0 AXIS-WIDTH))

     ;; z-axis (blue)
     (GL11/glColor3f 0.0 0.0 1.0)
     (util/do-shape GL11/GL_QUADS
               (GL11/glVertex3f 0.0 0.0 -1.0)
               (GL11/glVertex3f 0.0 0.0  1.0)
               (GL11/glVertex3f 0.0 AXIS-WIDTH  1.0)
               (GL11/glVertex3f 0.0 AXIS-WIDTH -1.0)))))
