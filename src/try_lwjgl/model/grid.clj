(ns try-lwjgl.model.grid
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]))

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
                  (GL11/glVertex3f 10 0 0))))))
