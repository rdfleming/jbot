package me.ramswaroop.jbot.core.slack.state;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateController {

    String state() default CurrentState.START_STATE;
    String pattern() default "";
}
