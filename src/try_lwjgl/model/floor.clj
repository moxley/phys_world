(ns try-lwjgl.model.floor
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.model.textured-panel :as textured-panel]))

(defn draw []
  (doseq [x (range -10 10)
          z (range -10 10)]
    (util/with-pushed-matrix
     (GL11/glTranslatef x -10 z)
     (GL11/glRotatef 90 1 0 0)
     (textured-panel/draw))))
