package org.example.demo3.event;
/*
T ist ein beliebiger Typ, der von Event erbt (also Event selbst oder ein Subtyp).
Das heißt:
Ein EventHandler<T> kann nur Events vom Subtyp T  verarbeiten.
 */

public interface EventHandler<T extends Event> {
    void execute(T event);
}