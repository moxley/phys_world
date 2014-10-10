(ns try-lwjgl.model.stairs
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.model.wood-block :as wood-block]))

(defn draw []
  (doseq [side [0 1 2 3]]
    (doseq [up [0 1 2]]
      (util/with-pushed-matrix
       (GL11/glTranslatef side up (* -1.0 up))
       (wood-block/draw)))))
