(defproject try_lwjgl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [hello_lwjgl/lwjgl "2.9.1"]
                 [shadertone/lwjgl-natives "2.9.0"]
                 [kephale/lwjgl "2.9.0"]
                 [slick-util "1.0.0"]
                 [oskar/lwjgl_util "1.0"]]

  :main ^:skip-aot try-lwjgl.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
