# COVID-19 (Coronavirus) ABM(Agent Based Model)  🦠

Agent based model and simulations of social strategies to stop the spread of covid-19 disease.


## How to start

Have Java in your machine; Adding all the jar files here for quick setup.


### Natural spread
Natural spread case run:
```
social_natural/infectionUI
```
![](https://raw.githubusercontent.com/codeAshu/covid-19-Multiagent-Simulations/master/videos/social_natural.gif)



### Simulation of social distancing
[Similar to the  Washington Post Article: Why outbreaks like coronavirus spread exponentially, and how to “flatten the curve” - Washington Post](https://www.washingtonpost.com/graphics/2020/world/corona-simulator/)

For social distancing model run:

```
social_dist/infectionUI
```

![](https://raw.githubusercontent.com/codeAshu/covid-19-Multiagent-Simulations/master/videos/social_distance.gif)


## Future work
- More sophisticated agent model.
- Test more lockdown strategies.
- Add tracing of parameters and ability to generate result graphs.
- Improve the code and suggestions to scale.


# Reference

This code is using Multi agent simulator [MASON](https://cs.gmu.edu/~eclab/projects/mason/).
This library has great multithreading
and nice framework to generate large visual simulations. It is still in old Java applet based framework, so it's hard to
show it on web.