(ns try-lwjgl.frame
  (:require [try-lwjgl.display :as display]))

(defn iteration [delta]
  (.stepSimulation @world (* delta 1000.0))

  (handle-input delta)
  (.stepSimulation @world 0)
  (display/draw))
