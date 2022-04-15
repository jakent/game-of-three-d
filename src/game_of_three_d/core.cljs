(ns game-of-three-d.core
  (:require [game-of-three-d.state :as state]
            [game-of-three-d.view :as view]
            [reagent.dom :as rdom]))

(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [view/hello] root-el)
    (.addEventListener js/document "keydown" state/on-key-down)))

(defn ^:export init []
  (mount-root))
