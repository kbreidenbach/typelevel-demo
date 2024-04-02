# Typelevel Demo

The idea behind this project was to produce a small reference application using some of the typelevel stack. 
I've enjoyed pure functional programming for many years, and type-level is my go-to stack. I find it allows 
me to build clean, performant, maintainable libraries and applications.

One departure was to use `decline` for configuration. I chose this as I like the way I can easily configure
environment variables to be used for configuration in a pure functional way.

The package structure has been designed to keep things logically separated so that the reader should easily 
be able to see specific functionality.

You may notice that I've tried to avoid "naked" primitives as parameters, and instead chosen to use Scala 3's
opaque types. I've found this to be a convenient way to improve type safety in code.

This is an evolving codebase and I hope to continually improve the code - suggestions are always welcome.