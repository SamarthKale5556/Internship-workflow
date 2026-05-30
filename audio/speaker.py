import pygame
import time

class VoiceAssistant:

    def __init__(self):

        pygame.mixer.init()

        self.sounds = {
            "person": pygame.mixer.Sound("audio_files/person_ahead.mp3"),
            "vehicle": pygame.mixer.Sound("audio_files/vehicle_ahead.mp3"),
            "obstacle": pygame.mixer.Sound("audio_files/obstacle_ahead.mp3"),
            "object": pygame.mixer.Sound("audio_files/object_ahead.mp3")
        }

        self.last_play_time = {}

        self.repeat_interval = 3

    def speak(self, category):

        current_time = time.time()

        last_time = self.last_play_time.get(category, 0)

        if current_time - last_time >= self.repeat_interval:

            print(f"[VOICE]: {category}")

            self.sounds[category].play()

            self.last_play_time[category] = current_time