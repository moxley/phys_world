(ns try-lwjgl.model.stairs
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.model.wood-block :as wood-block]))

(defn create-one [world position]
  (let [width 1
        phys (physics/build-box width position)
        specs {:width width
               :position position
               :phys phys
               :world world}]
    (.addRigidBody world phys)
    specs))

(defn create [world]
  (let [width 4
        height 3
        progression (for [y (range height) x (range width)] [x y])]
    (map (fn [i] (let [[side up] i] (create-one world [side up (* -1.0 up)])))
         progression)))

(defn draw [stairs]
  (doseq [stair stairs]
    (let [phys (:phys stair)
          width (:width stair)
          offset (/ width 2.0)
          pos (physics/get-position phys)
          [x y z] pos]
      (util/with-pushed-matrix
        (GL11/glTranslatef (- x offset) (- y offset) (+ z offset))
        (wood-block/draw)))))
