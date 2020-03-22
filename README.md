# COVID-19 (Coronavirus) ABM(Agent Based Model)  🦠

Agent model simulations of social strategies to stop the spread of covid-19 disease.

Simulation of social distancing.
[Similar to the  Washington Post Article: Why outbreaks like coronavirus spread exponentially, and how to “flatten the curve” - Washington Post](https://www.washingtonpost.com/graphics/2020/world/corona-simulator/)

## How to start

Have Java in your machine; Adding all the jar files here for quick setup.

For natural spread case run:
```
social_natural/infectionUI
```
![](videos/social_natural.mov)

For social distancing model run:

```
social_dist/infectionUI
```

![](videos/social_distance.mov)


## Future work
- More sophesticated agent model.
- Test more lockdown strategies.
- Add tracing of parameters and ability to generate result graphs.
- Improve the code and suggestions on how it be part of a blog.


#Reference
This code is using Multi agent simulator [MASON](https://cs.gmu.edu/~eclab/projects/mason/).
This library has great multithreading
and nice framework to generate large visual simulations. It is still in old Java applet based framework, so it's hard to
show it on web.