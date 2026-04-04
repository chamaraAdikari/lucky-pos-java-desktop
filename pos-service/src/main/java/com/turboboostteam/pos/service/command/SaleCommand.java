package com.turboboostteam.pos.service.command;

// Command Pattern — execute and undo
public interface SaleCommand {
    void execute();
    void undo();
    String getDescription();
}