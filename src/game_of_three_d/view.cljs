(ns game-of-three-d.view
  (:require ["react" :as react]
            ["@react-three/fiber" :refer (Canvas useFrame useThree extend)]
            ["@react-three/drei" :refer (Sphere Stars OrbitControls)]
            ["@react-three/postprocessing" :refer (EffectComposer DepthOfField Bloom Noise Vignette)]
    ;["three/examples/jsm/postprocessing/UnrealBloomPass" :refer (UnrealBloomPass)]
    ;["three/examples/jsm/postprocessing/EffectComposer" :refer (EffectComposer)]
    ;["three/examples/jsm/postprocessing/RenderPass" :refer (RenderPass)]
            [game-of-three-d.rules :as rules]
            [game-of-three-d.state :as state]
            [odoyle.rules :as o]
            [reagent.core :as r]))


(defn sphere [{[x y z] :coordinate}]
  (let [ref (react/useRef)]
    (useFrame (fn [_state _delta]
                (set! (.. ref -current -emissiveIntensity) 1)))
    [:mesh {:position   [x y z]
            :castShadow false}
     [:sphereGeometry {:args [0.4 25 25]}]
     [:meshPhysicalMaterial {:color             "gold"
                             :ref               ref
                             :roughness         0.5
                             :metalness         0.2
                             :emissiveIntensity 1
                             :emissive          "blue"
                             :wireframe         false}]]))

(defn box [{[x y z] :coordinate}]
  [:mesh {:position [x y z]}
   [:boxGeometry {:args [0.99 0.99 0.99]}]
   [:meshBasicMaterial {:color       "white"
                        :opacity     0.9
                        :transparent true}]])

(defn bloom [& children]
  (let [;three    (useThree)
        ref (react/useRef)]
    ;useFrame(() => ref.current && composer.current.render(), 1);
    ;(useFrame (fn [_state _delta]
    ;            (when (.. ref -current)
    ;              (.. composer -current render)))
    ;          1)
    [:<>
     (into [:scene {:ref ref}]
           children)
     [:> EffectComposer {:multisampling     0
                         ;:scene ref
                         :disableNormalPass true}
      [:> Bloom {:luminanceThreshold 0.3
                 :luminanceSmoothing 0.9
                 :height             1000
                 :opacity            1}]]]))

(defn grid []
  (let [session @(r/cursor state/app-state [:session])
        cells   (o/query-all session ::rules/cells)]
    (into [:<>]
          (for [cell cells]
            ^{:key (:coordinate cell)}
            [box cell]))))

(defn player []
  (let [session @(r/cursor state/app-state [:session])
        player  (first (o/query-all session ::rules/player))]
    [:f> bloom
     [:f> sphere player]]))

(defn canvas []
  [:> Canvas
   [:> OrbitControls {
                      :onChange (fn [] "change fired")
                      :onEnd    (fn [] "end fired")
                      :onStart  (fn [] "start fired")}]
   [:> Stars]
   [:ambientLight {:intensity 1}]
   ;[:spotLight {:position [10 15 10]
   ;             :angle    0.3}]
   [grid]
   [player]])

(defn ruleset-input []
  [:form {:onSubmit (fn [e]
                      (js/alert "foo" (.. e -event -target))
                      (. e preventDefault))}
   [:label {:for "ruleset"}
    "Rulesest:"
    [:input {:type "text"}]]
   [:input {:type  "submit"
            :value "Submit"}]])

(defn control-panel []
  (let [interval @(r/cursor state/app-state [:interval])]
    [:div {:style {:position         :absolute
                   :top              20
                   :left             30
                   :background-color :white}}
     [:button {:disabled (some? interval)
               :on-click state/tick}
      "Tick"]
     (if interval
       [:button {:on-click (state/pause-life interval)}
        "Stop"]
       [:button {:on-click state/start-life}
        "Start"])
     [:button {:disabled (some? interval)
               :on-click state/restart}
      "Restart"]
     #_[ruleset-input]]))

(defn hello []
  [:<>
   [canvas]
   [control-panel]])
