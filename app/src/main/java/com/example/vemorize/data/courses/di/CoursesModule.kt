package com.example.vemorize.data.courses.di

import com.example.vemorize.data.courses.AnnotationRepository
import com.example.vemorize.data.courses.SupabaseAnnotationRepositoryImpl
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.data.courses.SupabaseCourseTreeRepositoryImpl
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.data.courses.SupabaseCoursesRepositoryImpl
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
        impl: SupabaseCoursesRepositoryImpl
    ): CoursesRepository

    @Binds
    @Singleton
    abstract fun bindCourseTreeRepository(
        impl: SupabaseCourseTreeRepositoryImpl
    ): CourseTreeRepository

    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(
        impl: SupabaseAnnotationRepositoryImpl
    ): AnnotationRepository
}
