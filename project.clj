(defproject phys_world "0.1.0-SNAPSHOT"
  :description "A first-person exploration un-game"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [hello_lwjgl/lwjgl "2.9.1"]
                 [shadertone/lwjgl-natives "2.9.0"]
                 [kephale/lwjgl "2.9.0"]
                 [slick-util "1.0.0"]
                 [oskar/lwjgl_util "1.0"]
                 [org.clojars.charles-stain/jbullet "3.0"]
                 [org.clojars.nakkaya/vecmath "1"]]

  :main ^:skip-aot phys-world.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
