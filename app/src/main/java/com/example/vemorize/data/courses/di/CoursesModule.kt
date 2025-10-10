package com.example.vemorize.data.courses.di

import com.example.vemorize.data.courses.AnnotationRepository
import com.example.vemorize.data.courses.AnnotationRepositoryImpl
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.data.courses.CourseTreeRepositoryImpl
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.data.courses.CoursesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoursesModule {

    @Binds
    @Singleton
    abstract fun bindCoursesRepository(
        impl: CoursesRepositoryImpl
    ): CoursesRepository

    @Binds
    @Singleton
    abstract fun bindCourseTreeRepository(
        impl: CourseTreeRepositoryImpl
    ): CourseTreeRepository

    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(
        impl: AnnotationRepositoryImpl
    ): AnnotationRepository
}
