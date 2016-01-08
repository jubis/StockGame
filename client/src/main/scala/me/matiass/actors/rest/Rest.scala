package me.matiass.actors

import io.finch.Endpoint

trait Rest {
  def endpoint: Endpoint[_]
}
